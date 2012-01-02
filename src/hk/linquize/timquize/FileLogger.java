package hk.linquize.timquize;

import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.Calendar;

import android.content.*;
import android.content.pm.PackageManager.*;
import android.os.*;

public class FileLogger {
	static final SimpleDateFormat soFormat = new SimpleDateFormat("yyyyMMdd HHmmss:SSS");
	static String ssVersion;

	public static void writeLog(Context aoContext, String asText) {
		if (ssVersion == null) {
			try {
				ssVersion = Integer.toString(aoContext.getPackageManager()
						.getPackageInfo(aoContext.getPackageName(), 0).versionCode);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/timquize");
		dir.mkdirs();
		try {
			FileUtil.appendAllText(dir.getAbsolutePath() + "/log-" + ssVersion + ".txt",
					soFormat.format(Calendar.getInstance().getTime()) + "\t" + asText + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
