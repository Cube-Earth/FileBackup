package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.IOException;

import earth.cube.tools.file_backup.Globals;

public class TestDataDirectory {
	
	private File _dir;
	
	public TestDataDirectory(File dir) throws IOException {
		_dir = dir;
		_dir.mkdirs();
		createDatabase();
	}

	public File getFile(String sName) {
		return new File(_dir, sName);
	}
	
	public File getDatabaseFile() {
		return new File(_dir, Globals.HOUSEKEEPER_DIR_NAME + "/" + Globals.DATABASE_NAME);
	}
	
	
	private void createDatabase() throws IOException {
		File db = getDatabaseFile();
		db.getParentFile().mkdirs();
		StringUtil.writeToFile(db, "");
	}
	
	public File getDirectory() {
		return _dir;
	}

}
