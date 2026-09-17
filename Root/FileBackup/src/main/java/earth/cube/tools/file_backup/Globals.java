package earth.cube.tools.file_backup;

import java.io.File;
import java.text.SimpleDateFormat;

public class Globals {
	
	public final static String HOUSEKEEPER_DIR_NAME = ".housekeeper";
	public static final String DATABASE_NAME = "VolumeFiles.db";
	public final static String PROPERTIES_FILE_NAME = "VolumeFiles.properties";
	public final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public final static String DELETED_DIRECTORY = "/VolumeFiles/Deletions";

	public final static String ARCHIVE_DIRECTORY = "/VolumeFiles/Archive";

	
	public final static String BANNED_DIRECTORY_TOKEN = "@DeleteDirectories";
	public final static String BANNED_FILE_TOKEN = "@Delete";

	
	public static boolean isProtected(File file) {
		String s = file.getAbsolutePath();
		return s.endsWith("/" + HOUSEKEEPER_DIR_NAME) || s.indexOf("/" + HOUSEKEEPER_DIR_NAME + "/") != -1;
	}

	public static boolean shouldSkip(File file) {
		boolean bSkip = false;
		bSkip |= isProtected(file);
		bSkip |= file.isFile() && file.getName().equals(".DS_Store");
		return bSkip;
	}
	
}
