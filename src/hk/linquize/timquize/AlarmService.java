package hk.linquize.timquize;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.widget.Toast;

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
		writeLog("svc_onBind");
		return new AlarmBinder();
	}
	
	@Override
	public void onCreate () {
		super.onCreate();
		writeLog("svc_onCreate");
		
		moAlarms = new AlarmList(this, moAlarmListener);
        moPref = getSharedPreferences("Timquize", Context.MODE_PRIVATE);
        int liOfflineMinute = moPref.getInt("offlineTime", 23 * 60);
        mbOfflineEnabled = moPref.getBoolean("offlineEnabled", false);
        moCalOffline = CalendarUtil.setTimeOfDay(
    		CalendarUtil.setAsToday(Calendar.getInstance()), liOfflineMinute / 60, liOfflineMinute % 60);
        int liOnlineMinute = moPref.getInt("onlineTime", 8 * 60);
        mbOnlineEnabled = moPref.getBoolean("onlineEnabled", false);
        moCalOnline = CalendarUtil.setTimeOfDay(
    		CalendarUtil.setAsToday(Calendar.getInstance()), liOnlineMinute / 60, liOnlineMinute % 60);
    }
	
	@Override
	public void onDestroy () {
		super.onDestroy();
		writeLog("svc_onDestroy");
	}
	
	@Override
	public void onLowMemory () {
		super.onLowMemory();
		writeLog("svc_onLowMemory");
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		writeLog("svc_onStartCommand");
		changeAlarmTime(false, true, mbOfflineEnabled, moCalOffline.get(Calendar.HOUR), moCalOffline.get(Calendar.MINUTE));
		changeAlarmTime(false, false, mbOnlineEnabled, moCalOnline.get(Calendar.HOUR), moCalOnline.get(Calendar.MINUTE));
		return super.onStartCommand(intent, flags, startId);
	}
	
	AlarmList.OnAlarmListener moAlarmListener = new AlarmList.OnAlarmListener() {		
		public void onAlarm(String asName) {
			if ("offline".equals(asName)) {
	    		enableAirplaneMode();
	    		updateAlarmWithinOneDay(moCalOffline, "offline", moCalOffline.get(Calendar.HOUR_OF_DAY), moCalOffline.get(Calendar.MINUTE));
	    	}
	    	else if ("online".equals(asName)) {
	    		disableAirplaneMode();
	    		updateAlarmWithinOneDay(moCalOnline, "online", moCalOnline.get(Calendar.HOUR_OF_DAY), moCalOnline.get(Calendar.MINUTE));
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
	}
    
    public void disableAirplaneMode() {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", false);
		sendBroadcast(intent);
		showToastShortly(getString(R.string.goOnline));
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
	    	updateAlarmWithinOneDay(CalendarUtil.copyFromNow(loCalendar), 
				abIsOffline ? "offline" : "online", aiHourOfDay, aiMinute);
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
    
    void writeLog(String asText) {
    	if (msVersion == null) {
	        try {
				msVersion = Integer.toString(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	File sdCard = Environment.getExternalStorageDirectory();
    	File dir = new File (sdCard.getAbsolutePath() + "/timquize");
    	dir.mkdirs();
    	try {
			FileUtil.appendAllText(dir.getAbsolutePath() + "/log-" + msVersion + ".txt", 
					DateUtil.FormatLocalDateTimePrecise(Calendar.getInstance().getTime()) + "\t" + asText + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
