package earth.cube.tools.file_backup.commons;

public class EscapeUtil {
	
	public static String escapeSqlString(String s) {
		return s == null ? null : s.replaceAll("'", "''");
	}

}
