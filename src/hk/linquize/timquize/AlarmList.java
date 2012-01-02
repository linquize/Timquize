package hk.linquize.timquize;

import java.util.*;
import java.util.Map.*;
import java.util.AbstractMap.*;

import android.app.*;
import android.content.*;
import android.util.Log;

public class AlarmList {
	public abstract static interface OnAlarmListener {
		public abstract void onAlarm(String asName);
		public abstract void onCurrentAlarmChanged(String asName);		
	}
	
	Context moService;
	OnAlarmListener moListener;
	AlarmManager moAlarmManager;
	Map<String, Long> mmapAlarms;
	Entry<String, Long> mentPrevious = createDefaultEntry(), mentCurrent = createDefaultEntry();
	
	static final String ALARM_ACTION = "hk.linquize.timquize.ALARM_ACTION";
	
	BroadcastReceiver moReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (!ALARM_ACTION.equals(intent.getAction())) return;
            String lsName = intent.getStringExtra("name");
    		mentPrevious = cloneEntry(mentCurrent);
            remove(lsName);
            if (moListener != null)
            	moListener.onAlarm(lsName);
        }
	};
	
	public AlarmList(Context aoContext, OnAlarmListener aoListener) {
		moService = aoContext;
		moListener = aoListener;
		moAlarmManager = (AlarmManager)moService.getSystemService(Context.ALARM_SERVICE);
		mmapAlarms = new HashMap<String, Long>();
		aoContext.registerReceiver(moReceiver, new IntentFilter(ALARM_ACTION));
	}
	
	public void set(String asName, Calendar aoCalendar) {
		if (aoCalendar == null) throw new IllegalArgumentException();
		set(asName, aoCalendar.getTimeInMillis());
	}
	
	public void set(String asName, long alTime) {
		if (asName == null) throw new IllegalArgumentException();
		Long llOldTime = mmapAlarms.get(asName);
		Entry<String, Long> loEntry1 = llOldTime != null ? createDefaultEntry() : findMinTime();
		mmapAlarms.put(asName, alTime);
		Entry<String, Long> loEntry2 = findMinTime();
		if (loEntry2.getValue() != loEntry1.getValue())
			refreshAlarm(loEntry2.getKey(), loEntry2.getValue());
		else
		{
			Date loDate = new Date();
			loDate.setTime(loEntry1.getValue());
			Log.d("AlarmList.set()", loEntry1.getKey() + ", " + DateUtil.FormatLocalDateTime(loEntry1.getValue()));
		}
	}
	
	public void remove(String asName) {
		if (asName == null) throw new IllegalArgumentException();
		Entry<String, Long> loEntry1 = findMinTime();
		if (mmapAlarms.remove(asName) == null) return;
		Entry<String, Long> loEntry2 = findMinTime();
		if (loEntry2.getValue() != loEntry1.getValue())
			refreshAlarm(loEntry2.getKey(), loEntry2.getValue());
	}
	
	public Entry<String, Long> getPrevious() {
		return mentPrevious;
	}
	
	public Entry<String, Long> getCurrent() {
		return mentCurrent;
	}
	
	void refreshAlarm(String asName, long alTime) {
		Intent loIntent = new Intent(ALARM_ACTION);
		if (asName != null) {
			loIntent.putExtra("name", asName);
			Log.d("refreshAlarm()", asName + ", " + DateUtil.FormatLocalDateTime(alTime));
			mentCurrent = new SimpleEntry<String, Long>(asName, alTime);
			PendingIntent loPendingIntent = PendingIntent.getBroadcast(moService, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.set(AlarmManager.RTC, alTime, loPendingIntent);
		}
		else
		{
			mentCurrent = createDefaultEntry();
			PendingIntent loPendingIntent = PendingIntent.getBroadcast(moService, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.cancel(loPendingIntent);
		}

        if (moListener != null)
        	moListener.onCurrentAlarmChanged(asName);
	}
	
	Entry<String, Long> findMinTime() {
		Entry<String, Long> loEntry = createDefaultEntry();
		for (Entry<String, Long> lkvpAlarm : mmapAlarms.entrySet()) {
			if (loEntry.getValue() > lkvpAlarm.getValue()) {
				loEntry = cloneEntry(lkvpAlarm);
			}
		}
		return loEntry;
	}
	
	static Entry<String, Long> cloneEntry(Entry<String, Long> aoEntry) {
		return new SimpleEntry<String, Long>(aoEntry.getKey(), aoEntry.getValue());
	}
	
	static Entry<String, Long> createDefaultEntry() {
		return new SimpleEntry<String, Long>(null, Long.MAX_VALUE);
	}
}
