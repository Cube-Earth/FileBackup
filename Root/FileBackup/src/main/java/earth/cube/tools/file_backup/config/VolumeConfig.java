package earth.cube.tools.file_backup.config;

import java.io.File;
import java.io.IOException;

import earth.cube.tools.file_backup.database.FileDatabase;
import earth.cube.tools.file_backup.files.FileSystem;
import lombok.Getter;

public class VolumeConfig {
	
	@Getter
	private FileSystem _fileSystem;
	
	@Getter
	private FileSystem _wasteBin;
	
	@Getter
	private FileSystem _archive;
	
	@Getter
	private PropertiesX _properties;

	@Getter
	private FileDatabase _database;
	

	
	public void locate(File dir) throws IOException {
		VolumeLocator locator = new VolumeLocator();
		locator.findDatabase(dir);
		
		_fileSystem = new FileSystem(locator.getRootDirectory());
		_wasteBin = new FileSystem(new File(locator.getHousekeeperDirectory(), "VolumeFiles/Deletions"));
		_archive = new FileSystem(new File(locator.getHousekeeperDirectory(), "VolumeFiles/Archive"));
		
		_properties = new PropertiesX(locator.getPropertiesFile());
		
		_database = new FileDatabase(locator.getDatabaseFile());
		
		// TODO implement (Archive, Deleted, Database, etc.)
	}
	

}
