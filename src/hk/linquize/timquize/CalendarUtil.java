package hk.linquize.timquize;

import java.util.*;

public class CalendarUtil {
	public static int diffMinuteOfDay(Calendar aoCalendar, int aiHourOfDay, int aiMinute) {
		return diffMinuteOfDay_Impl(aoCalendar.get(Calendar.HOUR_OF_DAY), aiHourOfDay, aoCalendar.get(Calendar.MINUTE), aiMinute);
	}
    
	public static int diffMinuteOfDay(int aiHourOfDay, int aiMinute, Calendar aoCalendar) {
    	return diffMinuteOfDay_Impl(aiHourOfDay, aoCalendar.get(Calendar.HOUR_OF_DAY), aiMinute, aoCalendar.get(Calendar.MINUTE));
    }
	
	static int diffMinuteOfDay_Impl(int aiHour1, int aiHour2, int aiMinute1, int aiMinute2) {
		int liHourDiff = aiHour1 - aiHour2;
    	int liMinuteDiff = aiMinute1 - aiMinute2;
    	if (liMinuteDiff < 0) {
    		liMinuteDiff += 60;
    		liHourDiff--;
    	}
    	if (liHourDiff < 0)
    		liHourDiff += 24;
    	return liHourDiff * 60 + liMinuteDiff;
	}
	
	public static Calendar copyFromNow(Calendar aoCalendar) {
		aoCalendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		return aoCalendar;
	}
	
	public static Calendar setAsToday(Calendar aoCalendar) {
		Calendar loCalendar = Calendar.getInstance();
		aoCalendar.set(Calendar.YEAR, loCalendar.get(Calendar.YEAR));
		aoCalendar.set(Calendar.MONTH, loCalendar.get(Calendar.MONTH));
		aoCalendar.set(Calendar.DAY_OF_MONTH, loCalendar.get(Calendar.DAY_OF_MONTH));
		return aoCalendar;
	}
	
	public static Calendar setTimeOfDay(Calendar aoCalendar, int aiHourOfDay, int aiMinute) {
		aoCalendar.set(Calendar.HOUR_OF_DAY, aiHourOfDay);
		aoCalendar.set(Calendar.MINUTE, aiMinute);
		aoCalendar.set(Calendar.SECOND, 0);
		aoCalendar.set(Calendar.MILLISECOND, 0);
		return aoCalendar;
	}
	
	public static Calendar addMinutes(Calendar aoCalendar, int aiMinutes) {
		aoCalendar.add(Calendar.MINUTE, aiMinutes);
		return aoCalendar;
	}
	
	public static Calendar setComingTimeOfDay(Calendar aoCalendar, int aiHourOfDay) {
		return setComingTimeOfDay_Impl(aoCalendar, aiHourOfDay, 0, 0, 0);
	}
	
	public static Calendar setComingTimeOfDay(Calendar aoCalendar, int aiHourOfDay, int aiMinute) {
		return setComingTimeOfDay_Impl(aoCalendar, aiHourOfDay, aiMinute, 0, 0);
	}
	
	public static Calendar setComingTimeOfDay(Calendar aoCalendar, int aiHourOfDay, int aiMinute, int aiSecond) {
		return setComingTimeOfDay_Impl(aoCalendar, aiHourOfDay, aiMinute, aiSecond, 0);
	}
	
	public static Calendar setComingTimeOfDay(Calendar aoCalendar, int aiHourOfDay, int aiMinute, int aiSecond, int aiMillisecond) {
		return setComingTimeOfDay_Impl(aoCalendar, aiHourOfDay, aiMinute, aiSecond, aiMillisecond);
	}
	
	static Calendar setComingTimeOfDay_Impl(Calendar aoCalendar, int aiHourOfDay, int aiMinute, int aiSecond, int aiMillisecond) {
		boolean lbCarry = false;
		int liHourOfDay = aoCalendar.get(Calendar.HOUR_OF_DAY);
		if (aiHourOfDay < liHourOfDay)
			lbCarry = true;
		else if (aiHourOfDay == liHourOfDay) {
			int liMinute = aoCalendar.get(Calendar.MINUTE);	
			if (aiMinute < liMinute)
				lbCarry = true;
			else if (aiMinute == liMinute) {
				int liSecond = aoCalendar.get(Calendar.SECOND);	
				if (aiSecond < liSecond)
					lbCarry = true;
				else if (aiSecond == liSecond) {
					int liMillisecond = aoCalendar.get(Calendar.MILLISECOND);	
					if (aiMillisecond <= liMillisecond)
						lbCarry = true;
				}
			}
		}
		
		aoCalendar.set(Calendar.HOUR_OF_DAY, aiHourOfDay);
		aoCalendar.set(Calendar.MINUTE, aiMinute);
		aoCalendar.set(Calendar.SECOND, aiSecond);
		aoCalendar.set(Calendar.MILLISECOND, aiMillisecond);
		if (lbCarry)
			aoCalendar.add(Calendar.DAY_OF_MONTH, 1);
		return aoCalendar;
	}
}
