package earth.cube.tools.file_backup.files;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.database.FileDatabase;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;
import earth.cube.tools.file_backup.filesystem.CompoundFile;
import lombok.AccessLevel;
import lombok.Getter;

public class FileSystem {

	@Getter
	private File _baseDirectory;
	
	@Getter
	private FileDatabase _database;
	
	public FileSystem(File baseDir) throws IOException {
		_baseDirectory = baseDir;
		File dbFile = new File(_baseDirectory, Globals.HOUSEKEEPER_DIR_NAME + '/' + Globals.DATABASE_NAME);
		_database = new FileDatabase(dbFile, true);
	}
	
	public CompoundFile getFile(String sNeutralPath) throws IOException {
		return new CompoundFile(this, sNeutralPath, new File(_baseDirectory, sNeutralPath));
	}
	
	public CompoundFile getFile(File file) throws IOException {
		String sNeutralPath = FileUtil.getRelativePath(_baseDirectory, file);
		if(sNeutralPath == null)
			throw new IllegalStateException(String.format("'%s' is not underneath '%s'!", file.getAbsolutePath(), _baseDirectory.getAbsolutePath()));
		return new CompoundFile(this, sNeutralPath, file);		
	}
	
	
	public String getCachedSha256(IFacetedFile file) throws IOException, SQLException {
		String sSha256 = null;
		sSha256 = file.withFacet(ILinuxFile.class, f -> { return null; }, (String) null);
		// TODO query from database
		return sSha256;
	}
	
}
