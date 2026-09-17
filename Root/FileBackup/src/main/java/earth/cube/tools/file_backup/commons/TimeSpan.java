package earth.cube.tools.file_backup.commons;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSpan {
	
	private String _sDueTimeExpression;
	private long _nStartTime;
	private long _nStopTime;
	private long _nDueTime;
	private long _nElapsedTime;
	private String _sElapsedTime;
	
	
	public TimeSpan() {
	}
	
	public TimeSpan(String sDueTimeExpression) {
		_sDueTimeExpression = sDueTimeExpression;
	}
	
	private int getCalendarField(char cUnit) {
		int nField;
		switch(cUnit) {

			case 'y':
				nField = Calendar.YEAR;
				break;

			case 'M':
				nField = Calendar.MONTH;
				break;
				
			case 'd':
				nField = Calendar.DAY_OF_MONTH;
				break;
			
			case 'W':
				nField = Calendar.DAY_OF_WEEK;
				break;
			
			case 'w':
				nField = Calendar.WEEK_OF_MONTH;
				break;
			
			case 'h':
				nField = Calendar.HOUR_OF_DAY;
				break;

			case 'm':
				nField = Calendar.MINUTE;
				break;

			case 's':
				nField = Calendar.SECOND;
				break;

			case 'S':
				nField = Calendar.MILLISECOND;
				break;
				
			default:
				throw new IllegalArgumentException("Unknown unit: " + cUnit);
		}
		
		return nField;
	}
	
	protected long evaluate(String sExpression) {
		if(sExpression == null || sExpression.length() == 0)
			return 0;
		
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(_nStartTime);

		Pattern p = Pattern.compile("\\s*([0-9]+)([a-zA-Z])", 0);
		Matcher m = p.matcher(sExpression);
		int n = 0;
		int nQuantity;
		char cUnit;
		
		while(m.find()) {
			n = m.end();
			
			nQuantity = Integer.parseInt(m.group(1));
			cUnit = m.group(2).charAt(0);
			
			date.add(getCalendarField(cUnit), nQuantity);
		}
		
		if(n < sExpression.length()) {
			throw new IllegalArgumentException("Unexpected characters found in expression: " + sExpression);
		}
		
		return date.getTimeInMillis();
	}
	
	
	protected int increment(int nCounter, Calendar date, int nField, char cUnit, long nUnitInMillis, StringBuilder sb) {
		int i = (int) ((date.getTimeInMillis() - _nStartTime) / nUnitInMillis);
		if(nCounter < 3 && (i != 0 || nCounter != 0)) {
			date.add(nField, -i);
			if(sb.length() > 0)
				sb.append(' ');
			sb.append(i).append(cUnit);
			return 1;
		}
		return 0;
	}
	
	protected void calcElapsedTimeAsString() {
		StringBuilder sb = new StringBuilder();
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(_nStopTime);
		int i = 0;
		
		i += increment(i, date, Calendar.YEAR, 'y', 1000*60*60*24*365L, sb);
		i += increment(i, date, Calendar.MONTH, 'M', 1000*60*60*24*28L, sb);
		i += increment(i, date, Calendar.DAY_OF_YEAR, 'd', 1000*60*60*24L, sb);
		i += increment(i, date, Calendar.HOUR, 'h', 1000*60*60, sb);
		i += increment(i, date, Calendar.MINUTE, 'm', 1000*60, sb);
		i += increment(i, date, Calendar.SECOND, 's', 1000, sb);
		i += increment(i == 0 ? 1 : i, date, Calendar.MILLISECOND, 'S', 1, sb);
		_sElapsedTime = sb.toString();
	}
	
	
	public void start(long nStartMillis) {
		_nStartTime = nStartMillis > 0 ? nStartMillis : System.currentTimeMillis();
		_nDueTime = evaluate(_sDueTimeExpression);
	}
	
	public void start() {
		start(-1);
	}
	
	public void stop(long nStopMillis) {
		_nStopTime = nStopMillis > 0 ? nStopMillis : System.currentTimeMillis();
		_nElapsedTime = _nStopTime - _nStartTime;
	}

	public void stop() {
		stop(-1);
	}
	
	public long getDueTime() {
		return _nDueTime;
	}
	
	public long getElapsedTime() {
		return _nElapsedTime;
	}

	public String getElapsedTimeAsString() {
		if(_sElapsedTime == null)
			calcElapsedTimeAsString();
		return _sElapsedTime;
	}
	
	public boolean isDue() {
		return _nDueTime > 0 && System.currentTimeMillis() >= _nDueTime;
	}
	
	public static long getTime(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond, int nMillis) {
		Calendar cal = Calendar.getInstance();
		cal.set(nYear, nMonth, nDay, nHour, nMinute, nSecond);
		cal.set(Calendar.MILLISECOND, nMillis);
		return cal.getTimeInMillis();
	}

	public void checkIfDue() throws TimeSpanDueException {
		if(isDue())
			throw new TimeSpanDueException();
	}
}
