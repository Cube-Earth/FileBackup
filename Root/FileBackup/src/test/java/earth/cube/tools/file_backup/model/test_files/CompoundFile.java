package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import earth.cube.tools.file_backup.commons.AssertUtils;
import earth.cube.tools.file_backup.commons.Counter;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.ValidationError;
import earth.cube.tools.file_backup.commons.TestCaseValidationException;
import earth.cube.tools.file_backup.model.DataSource;
import earth.cube.tools.file_backup.model.LinuxFile;
import earth.cube.tools.file_backup.model.LinuxInode;
import earth.cube.tools.file_backup.object_mapper.Bean;
import earth.cube.tools.file_backup.object_mapper.BeanBuilder;
import earth.cube.tools.file_backup.object_mapper.adapter.ResultSetAdapter;
import earth.cube.tools.file_backup.object_mapper.annotations.Attribute;

public class CompoundFile {
	
	public static int _nInvalidFiles;

	public static int _nOprhanedInodes;

	protected String _sPath;
	
	protected long _nInode;

	protected Date _dModified;
	
	protected boolean _bValid;

	@Attribute(name="f_updated")
	protected Date _fileUpdated;
	
	protected Date _dCtime;
	
	protected long _nSize;
	
	protected String _sExtension;
	
	protected String _sSha256;
	
	protected Date _dSha256Updated;
	
	@Attribute(name="n_updated")
	protected Date _dInodeUpdated;

	
	protected Date convertDate(Date date) {
		if(!(date instanceof Timestamp))
//			throw new IllegalStateException("Unsupported class: " + date.getClass().getCanonicalName());
			return date;
		return new Date(((Timestamp) date).getTime());
	}
		
	protected static int verifyFiles(Connection conn, Set<String> files) throws IOException, SQLException {
		Set<String> processedFiles = new HashSet<>();
		Set<String> remainingFiles = new HashSet<>(files);
		
		Counter invalid = new Counter();
		
		String sQuery = "select PATH, VALID from file";
		Query.perform(conn, q -> q.query(sQuery).execute().process( rs -> {
			try {
				if(!rs.getBoolean("VALID"))
					invalid.increment();
				else {
					String sPath = rs.getString("PATH");
					if(processedFiles.contains(sPath))
						throw new IllegalStateException("Duplicate entry for '" + sPath + "'!");
					if(!remainingFiles.contains(sPath))
						throw new IllegalStateException("Unexpected entry for '" + sPath + "'!");
					remainingFiles.remove(sPath);
					processedFiles.add(sPath);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));
		
		if(remainingFiles.size() > 0)
			throw new IllegalStateException("Missing entries: '" + String.join("', '", remainingFiles) + "'!");

		return invalid.get();
	}

	protected static int verifyInodes(Connection conn, Set<String> inodes) throws IOException, SQLException {
		Set<String> processedInodes = new HashSet<>();
		Set<String> remainingInodes = new HashSet<>(inodes);
		
		Counter orphaned = new Counter();
		
		String sQuery = "select ID from inode";
		Query.perform(conn, q -> q.query(sQuery).execute().process( rs -> {
			try {
				String sId = rs.getString("ID");
				
				int n = Query.perform(conn, q2 -> q2.query("select count(*) as cnt from file where inode=%s", sId).execute().asInt("cnt"));
				if(n == 0)
					orphaned.increment();
				else {
					if(processedInodes.contains(sId))
						throw new IllegalStateException("Duplicate entry for '" + sId + "'!");
					if(!remainingInodes.contains(sId))
						throw new TestCaseValidationException(ValidationError.UNEXPECTED_INODE_DB_ENTRY, "Unexpected entry for '" + sId + "'!");
					remainingInodes.remove(sId);
					processedInodes.add(sId);
				}
				
			} catch (Exception e) {
				if(e instanceof IllegalStateException)
					throw (IllegalStateException) e;
				else
					throw new IllegalStateException(e);
			}
		}));
		
		if(remainingInodes.size() > 0)
			throw new IllegalStateException("Missing entries: '" + String.join("', '", remainingInodes) + "'!");
		
		return orphaned.get();
	}
	
	public static Map<String, CompoundFile> getCompoundFiles(Connection conn) throws Exception {
		Map<String,CompoundFile> compoundFiles = new HashMap<>();
		
		Set<String> files = new HashSet<>();
		Set<String> inodes = new HashSet<>();
		
		Bean<ResultSet, CompoundFile> bean = BeanBuilder.create(ResultSetAdapter.class, CompoundFile.class).build();
		
		String sQuery = "select f.PATH, f.INODE, f.MODIFIED, f.VALID, f.UPDATED as f_updated, n.CTIME, n.SIZE, n.EXTENSION, n.SHA256, n.SHA256_UPDATED, n.UPDATED as n_updated from file f, inode n where f.inode=n.id";
		Query.perform(conn, q -> q.query(sQuery).execute().process( rs -> {
			try {
				compoundFiles.put(rs.getString("PATH"), bean.adopt(rs));
				files.add(rs.getString("PATH"));
				inodes.add(rs.getString("INODE"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));
		
		_nInvalidFiles = verifyFiles(conn, files);
		_nOprhanedInodes = verifyInodes(conn, inodes);
		
		return compoundFiles;
	}


	public static int getTotalFiles(Connection conn) throws IOException, SQLException {
		String sQuery = "select count(*) as cnt from file";
		return Query.perform(conn, q -> q.query(sQuery).execute().asInt("cnt"));
	}

	public static int getTotalINodes(Connection conn) throws IOException, SQLException {
		String sQuery = "select count(*) as cnt from inode";
		return Query.perform(conn, q -> q.query(sQuery).execute().asInt("cnt"));
	}
	
	
	public void checkAgainstPyhsicalFile(File rootDir) throws IOException, SQLException {
		LinuxFile file = new LinuxFile(new File(rootDir, _sPath));

		if(file.isValid() != _bValid)
			throw new TestCaseValidationException(_bValid ? ValidationError.EXPECTED_FILE_VALID : ValidationError.EXPECTED_FILE_INVALID, _sPath);
		
		if(file.isValid()) {
			assertEquals(file.getNodeId(), _nInode, _sPath);

			switch(file.getLastModificationDate().compareTo(_dModified)) {
				case -1:
					throw new TestCaseValidationException(ValidationError.MODIFICATION_TIME_OLDER, _sPath);
				case +1:
					throw new TestCaseValidationException(ValidationError.MODIFICATION_TIME_NEWER, _sPath);
			}
			
			
			LinuxInode inode = file.getInode();
			
			switch(inode.getChangedTime().compareTo(_dCtime)) {
				case -1:
					throw new TestCaseValidationException(ValidationError.CHANGED_TIME_OLDER, _sPath);
				case +1:
					throw new TestCaseValidationException(ValidationError.CHANGED_TIME_NEWER, _sPath);
			}
			
			AssertUtils.assertValidationEquals(inode.getSize(), _nSize, ValidationError.SIZE);
			AssertUtils.assertValidationEquals(inode.getExtension(), _sExtension, ValidationError.EXTENSION);
			AssertUtils.assertValidationEquals(inode.getSha256(), _sSha256, ValidationError.SHA256);
			AssertUtils.assertValidationEquals(inode.getSha256Source(), DataSource.CALCULATED, ValidationError.SHA256_SOURCE);
			AssertUtils.assertValidationEquals(inode.getChangedTime(), _dSha256Updated, ValidationError.SHA256_UPDATED);
		}
		else {
			// file has been likely deleted
			assertNotEquals(0, _nInode);
		}
	}
}
