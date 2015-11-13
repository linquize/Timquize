package hk.linquize.timquize;

import java.util.Map.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	ToggleButton ynbOffline, ynbOnline;
	Button btnOffline, btnOnline, btnOfflineNow, btnOnlineNow;
	TextView lblPastTime, lblComingTime;
	String msVersion;
	AlarmService moService;
	TimeData moOfflineTime, moOnlineTime;
	boolean mbInit;

	static final String ALARM_SERVICE = "hk.linquize.timquize.ALARM_SERVICE";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		writeLog("act_Create");
		initializeComponent();

		startService(new Intent(this, AlarmService.class));
		bindService(new Intent(ALARM_SERVICE), moConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		writeLog("act_Start");
	}

	@Override
	protected void onResume() {
		super.onResume();
		writeLog("act_Resume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		writeLog("act_Pause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		writeLog("act_Stop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		writeLog("act_Destroy");
		unbindService(moConnection);
	}

	void initializeComponent() {
		setContentView(R.layout.main);

		ynbOffline = (ToggleButton) findViewById(R.id.ynbOffline);
		ynbOffline.setOnCheckedChangeListener(ynbOffOn_onCheckedChange);

		ynbOnline = (ToggleButton) findViewById(R.id.ynbOnline);
		ynbOnline.setOnCheckedChangeListener(ynbOffOn_onCheckedChange);

		btnOffline = (Button) findViewById(R.id.btnOffline);
		btnOffline.setEnabled(false);
		btnOffline.setOnClickListener(btnOffOn_onClick);

		btnOnline = (Button) findViewById(R.id.btnOnline);
		btnOnline.setEnabled(false);
		btnOnline.setOnClickListener(btnOffOn_onClick);

		btnOfflineNow = (Button) findViewById(R.id.btnOfflineNow);
		btnOfflineNow.setOnClickListener(btnOfflineNow_onClick);

		btnOnlineNow = (Button) findViewById(R.id.btnOnlineNow);
		btnOnlineNow.setOnClickListener(btnOnlineNow_onClick);

		lblPastTime = (TextView) findViewById(R.id.lblPastTime);
		lblComingTime = (TextView) findViewById(R.id.lblComingTime);
	}

	CompoundButton.OnCheckedChangeListener ynbOffOn_onCheckedChange = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			boolean lbIsOffline = buttonView == ynbOffline;
			if (mbInit) {
				TimeData loTD = lbIsOffline ? moOfflineTime : moOnlineTime;
				loTD.setEnabled(isChecked);
				if (lbIsOffline)
					moService.changeOfflineTime(loTD);
				else
					moService.changeOnlineTime(loTD);
			}
			Button loButton = lbIsOffline ? btnOffline : btnOnline;
			loButton.setEnabled(isChecked);
		}
	};

	View.OnClickListener btnOffOn_onClick = new View.OnClickListener() {
		public void onClick(final View v) {
			final boolean lbIsOffline = v == btnOffline;
			final TimeData loTD = lbIsOffline ? moOfflineTime : moOnlineTime;
			TimePickerDialog loDialog = new TimePickerDialog(MainActivity.this,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							loTD.setHourOfDay(hourOfDay);
							loTD.setMinute(minute);
							((Button) v).setText(String.format("%1$02d:%2$02d", hourOfDay, minute));
							if (lbIsOffline)
								moService.changeOfflineTime(loTD);
							else
								moService.changeOnlineTime(loTD);
						}
					}, loTD.getHourOfDay(), loTD.getMinute(), true);
			loDialog.show();
		}
	};

	View.OnClickListener btnOfflineNow_onClick = new View.OnClickListener() {
		public void onClick(View v) {
			moService.enableAirplaneMode();
		}
	};

	View.OnClickListener btnOnlineNow_onClick = new View.OnClickListener() {
		public void onClick(View v) {
			moService.disableAirplaneMode();
		}
	};

	ServiceConnection moConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			AlarmService.AlarmBinder loBinder = (AlarmService.AlarmBinder) service;
			moService = loBinder.getService();
			moOfflineTime = moService.getOfflineTime();
			moOnlineTime = moService.getOnlineTime();
			ynbOffline.setChecked(moOfflineTime.isEnabled());
			ynbOnline.setChecked(moOnlineTime.isEnabled());
			btnOffline.setText(String.format("%1$02d:%2$02d", moOfflineTime.getHourOfDay(), moOfflineTime.getMinute()));
			btnOnline.setText(String.format("%1$02d:%2$02d", moOnlineTime.getHourOfDay(), moOnlineTime.getMinute()));
			moService.addAlarmListener(moAlarmListener);
			mbInit = true;
			updatePastComing();
		}

		public void onServiceDisconnected(ComponentName name) {
			moService.removeAlarmListener(moAlarmListener);
			mbInit = false;
			moService = null;
		}
	};

	AlarmList.OnAlarmListener moAlarmListener = new AlarmList.OnAlarmListener() {
		public void onAlarm(String asName) {
			if ("offline".equals(asName)) {
			} else if ("online".equals(asName)) {
			}
		}

		public void onCurrentAlarmChanged(String asName) {
			updatePastComing();
		}
	};

	void updatePastComing() {
		Entry<String, Long> loPast = moService.getPreviousAlarm();
		String lsPastValue = loPast.getKey() == null ? "(N/A)"
				: (getResourceStringByKey(loPast.getKey()) + ": " + DateUtil.FormatLocalDateTime(loPast.getValue()));
		lblPastTime.setText(lsPastValue);

		Entry<String, Long> loCurrent = moService.getCurrentAlarm();
		String lsCurrentValue = loCurrent.getKey() == null ? "(N/A)" : (getResourceStringByKey(loCurrent.getKey())
				+ ": " + DateUtil.FormatLocalDateTime(loCurrent.getValue()));
		lblComingTime.setText(lsCurrentValue);
	}

	String getResourceStringByKey(String asName) {
		return getResourceStringByName("key_" + asName);
	}

	String getResourceStringByName(String asName) {
		int liId = getResources().getIdentifier(asName, "string", getPackageName());
		return liId == 0 ? null : getString(liId);
	}

	void writeLog(String asText) {
		FileLogger.writeLog(this, asText);
	}
}