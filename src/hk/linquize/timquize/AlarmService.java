package hk.linquize.timquize;

import java.util.*;
import java.util.Map.*;

import android.app.*;
import android.content.*;
import android.media.*;
import android.media.MediaPlayer.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.widget.*;

public class AlarmService extends Service {
	AlarmList moAlarms;
	Calendar moCalOffline, moCalOnline;
	boolean mbOfflineEnabled, mbOnlineEnabled;
	SharedPreferences moPref;
	String msVersion;
	ArrayList<AlarmList.OnAlarmListener> mlstAlarmListners = new ArrayList<AlarmList.OnAlarmListener>();

	public class AlarmBinder extends Binder {
		AlarmService getService() {
			return AlarmService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		writeLog("svc_Bind");
		return new AlarmBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		writeLog("svc_Create");

		moAlarms = new AlarmList(this, moAlarmListener);
		moPref = getSharedPreferences("Timquize", Context.MODE_PRIVATE);
		int liOfflineMinute = moPref.getInt("offlineTime", 23 * 60);
		mbOfflineEnabled = moPref.getBoolean("offlineEnabled", false);
		moCalOffline = CalendarUtil.setTimeOfDay(CalendarUtil.setAsToday(Calendar.getInstance()), liOfflineMinute / 60,
				liOfflineMinute % 60);
		int liOnlineMinute = moPref.getInt("onlineTime", 8 * 60);
		mbOnlineEnabled = moPref.getBoolean("onlineEnabled", false);
		moCalOnline = CalendarUtil.setTimeOfDay(CalendarUtil.setAsToday(Calendar.getInstance()), liOnlineMinute / 60,
				liOnlineMinute % 60);

		changeAlarmTime(false, true, mbOfflineEnabled, moCalOffline.get(Calendar.HOUR_OF_DAY),
				moCalOffline.get(Calendar.MINUTE));
		changeAlarmTime(false, false, mbOnlineEnabled, moCalOnline.get(Calendar.HOUR_OF_DAY),
				moCalOnline.get(Calendar.MINUTE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		writeLog("svc_Destroy");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		writeLog("svc_LowMemory");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		writeLog("svc_StartCmd");
		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isCallActive() {
		AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		return manager.getMode() == AudioManager.MODE_IN_CALL;
	}

	AlarmList.OnAlarmListener moAlarmListener = new AlarmList.OnAlarmListener() {
		public void onAlarm(String asName) {
			if ("offline".equals(asName)) {
				if (!isCallActive())
					enableAirplaneMode();
				updateAlarmWithinOneDay(moCalOffline, "offline", moCalOffline.get(Calendar.HOUR_OF_DAY),
						moCalOffline.get(Calendar.MINUTE));
			} else if ("online".equals(asName)) {
				disableAirplaneMode();
				updateAlarmWithinOneDay(moCalOnline, "online", moCalOnline.get(Calendar.HOUR_OF_DAY),
						moCalOnline.get(Calendar.MINUTE));
			}

			for (AlarmList.OnAlarmListener loAlarmListner : mlstAlarmListners)
				loAlarmListner.onAlarm(asName);
		}

		public void onCurrentAlarmChanged(String asName) {
			for (AlarmList.OnAlarmListener loAlarmListner : mlstAlarmListners)
				loAlarmListner.onCurrentAlarmChanged(asName);
		}
	};

	public void addAlarmListener(AlarmList.OnAlarmListener aoAlarmListener) {
		if (aoAlarmListener != null)
			mlstAlarmListners.add(aoAlarmListener);
	}

	public void removeAlarmListener(AlarmList.OnAlarmListener aoAlarmListener) {
		mlstAlarmListners.remove(aoAlarmListener);
	}

	public void enableAirplaneMode() {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		sendBroadcast(intent);

		showToastShortly(getString(R.string.goOffline));
		playSound();
	}

	public void disableAirplaneMode() {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", false);
		sendBroadcast(intent);

		showToastShortly(getString(R.string.goOnline));
		playSound();
	}

	public void changeOfflineTime(TimeData aoData) {
		changeAlarmTime(true, true, aoData.isEnabled(), aoData.getHourOfDay(), aoData.getMinute());
	}

	public void changeOnlineTime(TimeData aoData) {
		changeAlarmTime(true, false, aoData.isEnabled(), aoData.getHourOfDay(), aoData.getMinute());
	}

	public TimeData getOfflineTime() {
		TimeData loData = new TimeData();
		loData.setEnabled(mbOfflineEnabled);
		loData.setHourOfDay(moCalOffline.get(Calendar.HOUR_OF_DAY));
		loData.setMinute(moCalOffline.get(Calendar.MINUTE));
		return loData;
	}

	public TimeData getOnlineTime() {
		TimeData loData = new TimeData();
		loData.setEnabled(mbOnlineEnabled);
		loData.setHourOfDay(moCalOnline.get(Calendar.HOUR_OF_DAY));
		loData.setMinute(moCalOnline.get(Calendar.MINUTE));
		return loData;
	}

	public Entry<String, Long> getCurrentAlarm() {
		return moAlarms.getCurrent();
	}

	public Entry<String, Long> getPreviousAlarm() {
		return moAlarms.getPrevious();
	}

	void changeAlarmTime(boolean abIsUpdate, boolean abIsOffline, boolean abIsEnabled, int aiHourOfDay, int aiMinute) {
		Calendar loCalendar = abIsOffline ? moCalOffline : moCalOnline;

		if (abIsUpdate) {
			SharedPreferences.Editor loEditor = moPref.edit();
			loEditor.putInt(abIsOffline ? "offlineTime" : "onlineTime", aiHourOfDay * 60 + aiMinute);
			loEditor.putBoolean(abIsOffline ? "offlineEnabled" : "onlineEnabled", abIsEnabled);
			loEditor.commit();
		}

		if (abIsEnabled) {
			updateAlarmWithinOneDay(CalendarUtil.copyFromNow(loCalendar), abIsOffline ? "offline" : "online",
					aiHourOfDay, aiMinute);
		}
	}

	void updateAlarmWithinOneDay(Calendar aoCalendar, String asName, int aiHourOfDay, int aiMinute) {
		CalendarUtil.setComingTimeOfDay(aoCalendar, aiHourOfDay, aiMinute);
		Log.d(this.getClass().getSimpleName(), DateUtil.FormatLocalDateTimePrecise(aoCalendar.getTime()));
		moAlarms.set(asName, aoCalendar);
	}

	void showToastShortly(String asText) {
		Toast.makeText(this, asText, Toast.LENGTH_SHORT).show();
	}

	void playSound() {
		try {
			MediaPlayer loPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
			loPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			});
			loPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mp.release();
					return false;
				}
			});
			loPlayer.start();
		} catch (Exception e) {
		}
	}

	void writeLog(String asText) {
		FileLogger.writeLog(this, asText);
	}
}
