package earth.cube.tools.file_backup.commons;

public class TestUtil {
	
	public static String getMethodName(int nCaller) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		return stackTrace[nCaller].getMethodName();
	}

	public static String getMethodName() {
		return getMethodName(2);
	}

}
