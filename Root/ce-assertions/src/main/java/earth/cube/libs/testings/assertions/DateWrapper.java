package earth.cube.libs.testings.assertions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateWrapper extends Date {

	private static final long serialVersionUID = 1L;
	
	private final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	
	public DateWrapper(Date date) {
		super(date.getTime());
	}
	
	public DateWrapper(long nMillis) {
		super(nMillis);
	}
		
	@Override
	public String toString() {
		return DF.format(this);
	}
	
	public static DateWrapper from(Date date) {
		return new DateWrapper(date);
	}
	
	public void truncateMillis() {
		setTime(getTime() / 1000 * 1000);
	}
	
}
