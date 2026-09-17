package earth.cube.tools.file_backup.config;

import java.io.File;
import java.io.IOException;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.files.facets.impl.LinuxFile;
import lombok.Getter;


public class VolumeLocator {
	
	@Getter
	private File _databaseFile;
	
	@Getter
	private File _housekeeperDirectory;
	
	@Getter
	private File _rootDirectory;
	
	@Getter
	private File _propertiesFile;
	
	
	private void findDatabase(long nDeviceId, File dir) throws IOException {
		if(dir == null || nDeviceId != new LinuxFile(dir).getDeviceId())
			throw new IllegalStateException("No database found!");

		File dbFile = new File(dir, Globals.HOUSEKEEPER_DIR_NAME + "/" + Globals.DATABASE_NAME);
		if(dbFile.exists()) {
			_databaseFile = dbFile;
			_housekeeperDirectory = _databaseFile.getParentFile();
			_rootDirectory = _housekeeperDirectory.getParentFile();
			_propertiesFile = new File(_housekeeperDirectory, Globals.PROPERTIES_FILE_NAME);
			return;
		}
		
		findDatabase(nDeviceId, dir.getParentFile());
	}
	
	public void findDatabase(File dir) throws IOException {
		findDatabase(new LinuxFile(dir).getDeviceId(), dir);
	}

}
