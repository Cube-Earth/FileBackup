package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.model.test_files.FileSystem;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class DatabaseNumberChecker {
	
	public static DatabaseNumberChecker from(Scope scope) {
		return new DatabaseNumberChecker(scope);
	}

	
	protected Scope _scope;

	@Setter
	protected FileSystem _fileSystem;
	
	@Setter
	protected int _nBanned;
	
	@Setter
	protected int _nSharedInodes;
	
	@Setter
	protected int _bBannedDirectoryEntries;
	
	@Setter
	protected int _nBannedDirectoryTriggers;
	
	
	public DatabaseNumberChecker(Scope scope) {
		_scope = scope;
	}

	
	public void check() throws Exception {	
		int nOrphanedFiles = _fileSystem.getHardDeletionCount() + _fileSystem.getSoftDeletionCount() + _fileSystem.getArchiveRemovalCount();
		
		int nGeneratedFiles = _fileSystem.getGeneratedFileCount();

		int nTotalFiles = _fileSystem.getFileCount() + nOrphanedFiles - nGeneratedFiles;

		int nTotalInodes = nTotalFiles;
		
		int nActiveFiles = _fileSystem.getFileCount() - nGeneratedFiles;
		
		int nOrphanedInodes = _fileSystem.getOrphanedInodeCount();
		
		
		_fileSystem.checkAll(_scope.getConnection(), nOrphanedFiles, nOrphanedInodes);
		
		assertEquals(nTotalFiles, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(nActiveFiles, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(nTotalInodes, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(nActiveFiles, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		assertEquals(_nSharedInodes, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(_nBanned, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));

		assertEquals(_bBannedDirectoryEntries, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_directory_file").execute().asInt("cnt")));
				
		assertEquals(_nBannedDirectoryTriggers, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_directory_file where trigger=1").execute().asInt("cnt")));
	}
	
}
