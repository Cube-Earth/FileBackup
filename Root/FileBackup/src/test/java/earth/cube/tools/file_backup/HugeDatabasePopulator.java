package earth.cube.tools.file_backup;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.database.FileDatabase;
import earth.cube.tools.file_backup.model.InodeKey;
import earth.cube.tools.file_backup.model.RecordFileSaver;
import earth.cube.tools.file_backup.model.RecordInodeSaver;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HugeDatabasePopulator {
	
	public final static int NUM_FILES = 200_000;
	public final static int NUM_INODES = 200_000;
	public final static int NUM_SHA256 = 100;
	
	private File _file;
	
	private File _indicatorFile;
	
	
	public HugeDatabasePopulator(String sId) {
		_file = new File(TestGlobals.TEST_DATA_GEN_DIR, sId + ".db");
		_indicatorFile = new File(TestGlobals.TEST_DATA_GEN_DIR, sId + ".finalized");
		_file.getParentFile().mkdirs();
	}
	
	public void populate() throws IOException, SQLException {
		String sId = FileUtil.readAsString(_indicatorFile);
		if(!getId().equals(sId)) {
			FileUtil.deleteFile(_file);
			
			int m = Math.max(NUM_FILES, NUM_INODES);
			
			try(FileDatabase db = new FileDatabase(_file)) {
				Connection conn = db.getConnection();
				for(int i = 0; i < NUM_FILES; i++) {
					RecordFileSaver.create(conn).path(getFilePath(i)).inodeId(i % NUM_INODES).lastModified(new Date()).updatedTime(new Date()).save();
					if(i < NUM_INODES) {
						InodeKey key = getNodeKey(i);
						RecordInodeSaver.create(conn).id(getNodeId(i)).size(key.getSize()).extension(key.getExension()).changedTime(new Date()).sha256(key.getSha256()).sha256Updated(new Date()).updatedTime(new Date()).save();
					}
					if(i % 100 == 0 && log.isDebugEnabled()) {
						log.debug("populate: {}% created ({}/{})", i / m * 100, i, m);
					}
				}
				for(int i = NUM_FILES; i < NUM_INODES; i++) {
					InodeKey key = getNodeKey(i);
					RecordInodeSaver.create(conn).id(getNodeId(i)).size(key.getSize()).extension(key.getExension()).changedTime(new Date()).sha256(key.getSha256()).sha256Updated(new Date()).updatedTime(new Date()).save();
					if(i % 100 == 0 && log.isDebugEnabled()) {
						log.debug("populate: {}% created ({}/{})", i / m * 100, i, m);
					}
				}
			}
			
			FileUtil.write(_indicatorFile, getId());
		}
	}
	
	public File getFile() throws IOException, SQLException {
		populate();
		return _file;
	}
	
	protected String getId() {
		return NUM_FILES + "," + NUM_INODES + ',' + NUM_SHA256;
	}
	
	public static String getFilePath(int i) {
		return "/test/" + i + ".txt";
	}

	public static InodeKey getNodeKey(int i) {
		return new InodeKey("abcd" + (i % NUM_SHA256), ".txt", 5);
	}
	
	public static long getNodeId(int i) {
		return i % NUM_INODES;
	}
}
