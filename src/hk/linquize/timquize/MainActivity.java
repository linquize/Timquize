package hk.linquize.timquize;

import java.util.*;
import java.util.Map.Entry;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	ToggleButton ynbOffline, ynbOnline;
	Button btnOffline, btnOnline, btnOfflineNow, btnOnlineNow;
	TextView lblPastTime, lblComingTime;
	AlarmList moAlarms;
	Calendar moCalOffline, moCalOnline;
	SharedPreferences moPref;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponent();
        
        moAlarms = new AlarmList(this, moAlarmListener); 
        moPref = getSharedPreferences("Timquize", Context.MODE_PRIVATE);
        int liOfflineMinute = moPref.getInt("offlineTime", 23 * 60);
        moCalOffline = CalendarUtil.setTimeOfDay(
    		CalendarUtil.setAsToday(Calendar.getInstance()), liOfflineMinute / 60, liOfflineMinute % 60);
        int liOnlineMinute = moPref.getInt("onlineTime", 8 * 60);
        moCalOnline = CalendarUtil.setTimeOfDay(
    		CalendarUtil.setAsToday(Calendar.getInstance()), liOnlineMinute / 60, liOnlineMinute % 60);
        ynbOffline.setChecked(moPref.getBoolean("offlineEnabled", false));
        ynbOnline.setChecked(moPref.getBoolean("offlineEnabled", false));
        changeAlarmTime(false, true, moCalOffline.get(Calendar.HOUR_OF_DAY), moCalOffline.get(Calendar.MINUTE));
        changeAlarmTime(false, false, moCalOnline.get(Calendar.HOUR_OF_DAY), moCalOnline.get(Calendar.MINUTE));
    }
    
    void initializeComponent() {
        setContentView(R.layout.main);
        
        ynbOffline = (ToggleButton)findViewById(R.id.ynbOffline);
        ynbOffline.setOnCheckedChangeListener(ynbOffOn_onCheckedChange);
        
        ynbOnline = (ToggleButton)findViewById(R.id.ynbOnline);
        ynbOnline.setOnCheckedChangeListener(ynbOffOn_onCheckedChange);
        
        btnOffline = (Button)findViewById(R.id.btnOffline);
        btnOffline.setEnabled(false);
        btnOffline.setOnClickListener(btnOffOn_onClick);
        
        btnOnline = (Button)findViewById(R.id.btnOnline);
        btnOnline.setEnabled(false);
        btnOnline.setOnClickListener(btnOffOn_onClick);
        
        btnOfflineNow = (Button)findViewById(R.id.btnOfflineNow);
        btnOfflineNow.setOnClickListener(btnOfflineNow_onClick);
        
        btnOnlineNow = (Button)findViewById(R.id.btnOnlineNow);
        btnOnlineNow.setOnClickListener(btnOnlineNow_onClick);
        
        lblPastTime = (TextView)findViewById(R.id.lblPastTime);
        lblComingTime = (TextView)findViewById(R.id.lblComingTime);
    }
    
    CompoundButton.OnCheckedChangeListener ynbOffOn_onCheckedChange = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			boolean lbIsOffline = buttonView == ynbOffline;
			Button loButton = lbIsOffline ? btnOffline : btnOnline;
			Calendar loCalendar = lbIsOffline ? moCalOffline : moCalOnline;
			String lsName = lbIsOffline ? "offline" : "online";
			
			loButton.setEnabled(isChecked);
			if (isChecked) {
				int liHourOfDay = loCalendar.get(Calendar.HOUR_OF_DAY);
				int liMinute = loCalendar.get(Calendar.MINUTE);
				SharedPreferences.Editor loEditor = moPref.edit();
				loEditor.putBoolean(lbIsOffline ? "offlineEnabled" : "onlineEnabled", true);
				loEditor.commit();
				updateAlarmWithinOneDay(CalendarUtil.copyFromNow(loCalendar), lsName, liHourOfDay, liMinute);
			}
			else {
				SharedPreferences.Editor loEditor = moPref.edit();
				loEditor.putBoolean(lbIsOffline ? "offlineEnabled" : "onlineEnabled", false);
				loEditor.commit();
				moAlarms.remove(lsName);
			}
		}
    };
    
    View.OnClickListener btnOffOn_onClick = new View.OnClickListener() {
		public void onClick(final View v) {
			final boolean lbIsOffline = v == btnOffline;
			final Calendar loCalendar = lbIsOffline ? moCalOffline : moCalOnline;
			TimePickerDialog loDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
		        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		        	changeAlarmTime(true, lbIsOffline, hourOfDay, minute);
		        }
		    }, loCalendar.get(Calendar.HOUR_OF_DAY), loCalendar.get(Calendar.MINUTE), true);
			loDialog.show();
		}
    };
    
    View.OnClickListener btnOfflineNow_onClick = new View.OnClickListener() {
		public void onClick(View v) {
			enableAirplaneMode();
		}
    };
    
    View.OnClickListener btnOnlineNow_onClick = new View.OnClickListener() {
		public void onClick(View v) {
			disableAirplaneMode();
		}
    };
    

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
		}
		
		public void onCurrentAlarmChanged(String asName) {
			updatePastComing();
		}
	};
	
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    void updatePastComing() {
    	Entry<String, Long> loPast = moAlarms.getPrevious();
		lblPastTime.setText(loPast.getKey() + ": " + DateUtil.FormatLocalDateTime(loPast.getValue()));
		
		Entry<String, Long> loCurrent = moAlarms.getCurrent();
		lblComingTime.setText(loCurrent.getKey() + ": " + DateUtil.FormatLocalDateTime(loCurrent.getValue()));
    }
    
    void enableAirplaneMode() {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		sendBroadcast(intent);
		showToastShortly(getString(R.string.goOffline));
    }
    
    void disableAirplaneMode() {
		Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", false);
		sendBroadcast(intent);
		showToastShortly(getString(R.string.goOnline));
	}
    
    void changeAlarmTime(boolean abIsUpdate, boolean abIsOffline, int aiHourOfDay, int aiMinute) {
    	Button loButton = abIsOffline ? btnOffline : btnOnline;
    	ToggleButton loToggle = abIsOffline ? ynbOffline : ynbOnline;
    	Calendar loCalendar = abIsOffline ? moCalOffline : moCalOnline;
    	loButton.setText(String.format("%1$02d:%2$02d", aiHourOfDay, aiMinute));
    	
    	if (abIsUpdate) {
			SharedPreferences.Editor loEditor = moPref.edit();
			loEditor.putInt(abIsOffline ? "offlineTime" : "onlineTime", aiHourOfDay * 60 + aiMinute);
			loEditor.commit();
    	
	    	if (loToggle.isChecked()) {
		    	int liMinutes = CalendarUtil.diffMinuteOfDay(aiHourOfDay, aiMinute, Calendar.getInstance());
				String lsText = String.format("%1$s %2$02d:%3$02d %4$s", 
					getString(abIsOffline ? R.string.lblOffTime : R.string.lblOnTime),
					liMinutes / 60, liMinutes % 60, getString(R.string.after));
				showToastShortly(lsText);
				updateAlarmWithinOneDay(CalendarUtil.copyFromNow(loCalendar), 
					abIsOffline ? "offline" : "online", aiHourOfDay, aiMinute);
	    	}
    	}
    }
    
    void updateAlarmWithinOneDay(Calendar aoCalendar, String asName, int aiHourOfDay, int aiMinute) {
    	CalendarUtil.setComingTimeOfDay(aoCalendar, aiHourOfDay, aiMinute);
    	Log.d(this.getClass().getSimpleName(), DateUtil.FormatLocalDateTimePrecise(aoCalendar.getTime()));
    	moAlarms.set(asName, aoCalendar);
    }
    
    /**
     * Cross-thread safe way to show toast shortly.
     * @param asText
     */
    void showToastShortly(final String asText) {
    	this.runOnUiThread(new Runnable() {
    		public void run() {
    			Toast.makeText(MainActivity.this, asText, Toast.LENGTH_SHORT).show();
    		}
    	});
    }
}