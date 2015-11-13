package hk.linquize.timquize;

import java.text.*;
import java.util.*;

class DateUtil {
	public static String FormatLocalDateTime(long alDateTime) {
		Date loDate = new Date();
		loDate.setTime(alDateTime);
		return FormatLocalDateTime(loDate);
	}
	
	public static String FormatLocalDateTime(Date aoDateTime) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(aoDateTime);
	}
	
	public static String FormatLocalDateTimePrecise(Date aoDateTime) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(aoDateTime);
	}
}