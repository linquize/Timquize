package hk.linquize.timquize;

import java.util.*;
import java.util.Map.*;
import java.util.AbstractMap.*;

import android.app.*;
import android.content.*;

public class AlarmList {
	Activity moActivity;
	AlarmManager moAlarmManager;
	Map<String, Long> mmapAlarms;
	
	public AlarmList(Activity aoActivity) {
		moActivity = aoActivity;
		moAlarmManager = (AlarmManager)moActivity.getSystemService(Context.ALARM_SERVICE);
		mmapAlarms = new HashMap<String, Long>();
	}
	
	public void set(String asName, Calendar aoCalendar) {
		if (aoCalendar == null) throw new IllegalArgumentException();
		set(asName, aoCalendar.getTimeInMillis());
	}
	
	public void set(String asName, long alTime) {
		if (asName == null) throw new IllegalArgumentException();
		Entry<String, Long> loEntry1 = findMinTime();
		mmapAlarms.put(asName, alTime);
		Entry<String, Long> loEntry2 = findMinTime();
		if (loEntry1.getValue() > loEntry2.getValue())
			refreshAlarm(loEntry2.getKey(), loEntry2.getValue());
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
		Intent loIntent = new Intent(moActivity, moActivity.getClass());
		loIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (asName != null) {
			loIntent.putExtra("name", asName);
			PendingIntent loPendingIntent = PendingIntent.getActivity(moActivity, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.set(AlarmManager.RTC, alTime, loPendingIntent);
		}
		else
		{
			PendingIntent loPendingIntent = PendingIntent.getActivity(moActivity, 0, loIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			moAlarmManager.cancel(loPendingIntent);
		}
	}
	
	Entry<String, Long> findMinTime() {
		Entry<String, Long> loEntry = new SimpleEntry<String, Long>(null, Long.MAX_VALUE);
		for (Entry<String, Long> lkvpAlarm : mmapAlarms.entrySet()) {
			if (loEntry.getValue() > lkvpAlarm.getValue()) {
				loEntry = lkvpAlarm;
			}
		}
		return loEntry;
	}
}
