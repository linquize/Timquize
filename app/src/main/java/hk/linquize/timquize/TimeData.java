package hk.linquize.timquize;

public class TimeData {
	boolean mbEnabled;
	int miHourOfDay;
	int miMinute;
	
	public boolean isEnabled() {
		return mbEnabled;
	}
	
	public void setEnabled(boolean abEnabled) {
		mbEnabled = abEnabled;
	}
	
	public int getHourOfDay() {
		return miHourOfDay;
	}
	
	public void setHourOfDay(int aiHourOfDay) {
		miHourOfDay = aiHourOfDay;
	}

	public int getMinute() {
		return miMinute;
	}

	public void setMinute(int aiMinute) {
		miMinute = aiMinute;
	}
}
