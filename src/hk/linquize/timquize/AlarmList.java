package hk.linquize.timquize;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.*;
import java.util.AbstractMap.*;

import android.app.*;
import android.content.*;
import android.util.Log;

public class AlarmList {
	public abstract static class OnAlarmListener {
		public abstract void onAlarm(String asName);
	}
	
	Activity moActivity;
	OnAlarmListener moListener;
	AlarmManager moAlarmManager;
	Map<String, Long> mmapAlarms;
	
	final String ACTION_ALARM = "hk.linquize.timquize.ALARM";
	
	BroadcastReceiver moReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (!ACTION_ALARM.equals(intent.getAction())) return;
            String lsName = intent.getStringExtra("name");
            remove(lsName);
            if (moListener != null)
            	moListener.onAlarm(lsName);
        }
	};
	
	public AlarmList(Activity aoActivity, OnAlarmListener aoListener) {
		moActivity = aoActivity;
		moListener = aoListener;
		moAlarmManager = (AlarmManager)moActivity.getSystemService(Context.ALARM_SERVICE);
		mmapAlarms = new HashMap<String, Long>();
		aoActivity.registerReceiver(moReceiver, new IntentFilter(ACTION_ALARM));
	}
	
	public void set(String asName, Calendar aoCalendar) {
		if (aoCalendar == null) throw new IllegalArgumentException();
		set(asName, aoCalendar.getTimeInMillis());
	}
	
	public void set(String asName, long alTime) {
		if (asName == null) throw new IllegalArgumentException();
		Long llOldTime = mmapAlarms.get(asName);
		Entry<String, Long> loEntry1 = llOldTime != null ? 
				new SimpleEntry<String, Long>(null, Long.MAX_VALUE) : findMinTime();
		mmapAlarms.put(asName, alTime);
		Entry<String, Long> loEntry2 = findMinTime();
		if (loEntry1.getValue() > loEntry2.getValue())
			refreshAlarm(loEntry2.getKey(), loEntry2.getValue());
		else
		{
			Date loDate = new Date();
			loDate.setTime(loEntry1.getValue());			
			Log.d("AlarmList.set()", loEntry1.getKey() + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(loDate));
		}
	}
	
	public void remove(String asName) {
		if (asName == null) throw new IllegalArgumentException();
		Entry<String, Long> loEntry1 = findMinTime();
		if (mmapAlarms.remove(asName) == null) return;
		Entry<String, Long> loEntry2 = findMinTime();
		if (loEntry1.getValue() > loEntry2.getValue())
			refreshAlarm(loEntry2.getKey(), loEntry2.getValue());
	}
	
	void refreshAlarm(String asName, long alTime) {
		Intent loIntent = new Intent(ACTION_ALARM);
		if (asName != null) {
			loIntent.putExtra("name", asName);
			Date loDate = new Date();
			loDate.setTime(alTime);
			Log.d("refreshAlarm()", asName + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(loDate));
			PendingIntent loPendingIntent = PendingIntent.getBroadcast(moActivity, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.set(AlarmManager.RTC, alTime, loPendingIntent);
		}
		else
		{
			PendingIntent loPendingIntent = PendingIntent.getBroadcast(moActivity, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.cancel(loPendingIntent);
		}
	}
	
	Entry<String, Long> findMinTime() {
		Entry<String, Long> loEntry = new SimpleEntry<String, Long>(null, Long.MAX_VALUE);
		for (Entry<String, Long> lkvpAlarm : mmapAlarms.entrySet()) {
			if (loEntry.getValue() > lkvpAlarm.getValue()) {
				loEntry = new SimpleEntry<String, Long>(lkvpAlarm.getKey(), lkvpAlarm.getValue());
			}
		}
		return loEntry;
	}
}
