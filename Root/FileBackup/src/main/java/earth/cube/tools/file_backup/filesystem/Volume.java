package earth.cube.tools.file_backup.filesystem;

import java.io.File;

import earth.cube.tools.file_backup.Globals;

public class Volume {
	
	public boolean isProtected(File file) {
		String s = file.getAbsolutePath();
		return s.endsWith("/" + Globals.HOUSEKEEPER_DIR_NAME) || s.indexOf("/" + Globals.HOUSEKEEPER_DIR_NAME + "/") != -1;
	}	

}
