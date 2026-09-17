package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.DebugScope;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.IFuncConsume;
import earth.cube.tools.file_backup.commons.MemoryPrintStream;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.TestDataDirectory;
import earth.cube.tools.file_backup.commons.TestDataDirectoryBuilder;
import earth.cube.tools.file_backup.database.RecordBannedFile;
import earth.cube.tools.file_backup.model.LinuxFile;
import earth.cube.tools.file_backup.model.RecordFile;
import earth.cube.tools.file_backup.model.test_files.FileSystem;

public abstract class AbstractActionTest {
	
	protected TestDataDirectory _testDataDir;
	protected File _rootDir;
	protected Scope _scope;
	protected FileSystem _fs;
	protected String _sOut;
	
	
	protected File getExistingDatabaseFile() throws IOException, SQLException {
		return null;
	}
	
	
	@BeforeEach
	public void setUp(TestInfo info) throws IOException, SQLException {
		_testDataDir = TestDataDirectoryBuilder.instance(info).build();
		_rootDir = _testDataDir.getDirectory();		
		assertEquals(1, FileUtil.countFiles(_rootDir)); // database file
		assertEquals(0, _testDataDir.getDatabaseFile().length());
		
		File file = getExistingDatabaseFile();
		if(file != null) {
			Files.copy(file.toPath(), _testDataDir.getDatabaseFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
		_scope = new Scope(_rootDir);
		_fs = new FileSystem(_rootDir);
		DebugScope.reset();
		System.out.println("=== " + info.getDisplayName() + " ==============================");
	}
	
	protected <T extends IAction> void executeAction(Class<T> clazz, IFuncConsume<T> func) throws Exception {
		try(MemoryPrintStream out = new MemoryPrintStream()) {
			ActionParameters params = new ActionParameters();
			params.setSourceScope(_scope);
			params.setOut(out);
			
			T action = clazz.newInstance();
			action.setParameters(params);
			
			if(func != null) {
				func.consume(action);
			}
			
			action.execute();
			_sOut = out.getString();
		}
		_scope.cleanUp();
		_scope.resetCache();
	}
	
	
	protected void executeRegisterFilesAction() throws Exception {
		executeAction(RegisterFilesInDatabaseAction.class, action -> action.getParameters().setSourceDirectory(_rootDir));
	}

	protected void executeDeduplicateAllFilesAction() throws Exception {
		executeAction(DeduplicateAllFilesAction.class, action -> action.getParameters().setSourceDirectory(_rootDir));
	}

	protected void executeDeduplicateMostFilesAction() throws Exception {
		executeAction(DeduplicateMostFilesAction.class, action -> action.getParameters().setSourceDirectory(_rootDir));
	}
	
	protected void executeBanFilesAction(String sRelPathToBan) throws Exception {
		executeAction(BanFilesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPathToBan)));
	}

	protected void executeRemoveTheseFilesAction(String sRelPath) throws Exception {
		executeAction(RemoveTheseDuplicatesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPath)));
	}

	protected void executeRemoveOtherFilesAction(String sRelPath) throws Exception {
		executeAction(RemoveOtherDuplicatesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPath)));
	}

	protected void executeRemoveAllOtherFilesAction(String sRelPath) throws Exception {
		executeAction(RemoveAllOtherDuplicatesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPath)));
	}

	protected void executeFindClonedDirectoriesAction(String sRelPath) throws Exception {
		executeAction(FindClonedDirectoriesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPath)));
	}

	protected void executeShowDuplicatesAction(String sRelPath) throws Exception {
		executeAction(ShowDuplicatesAction.class, action -> action.getParameters().setSourceDirectory(new File(_scope.getRootDirectory(), sRelPath)));
	}

	protected void checkCounts(FileSystem fs, int nFileCount, int nInodeCount) throws IOException, SQLException {
		System.out.println("--------------------------------------");
		assertEquals(nFileCount, fs.getFileCount());
		assertEquals(fs.getFileCount() + 1, FileUtil.countFiles(_rootDir));

		assertEquals(nInodeCount, fs.getActiveInodeCount());
	}
	
	protected LinuxFile file(String sPath) throws IOException, SQLException {
		File file = new File(_scope.getRootDirectory(), sPath);
		LinuxFile linuxFile = new LinuxFile(file);
		return linuxFile;
	}
	
	protected RecordFile getRecordFile(String sPath) throws IOException, SQLException {
		File file = new File(_scope.getRootDirectory(), sPath);
		RecordFile rfile = new RecordFile(_scope, file);
		return rfile;
	}
	
	public void registerFiles() throws Exception {
		_fs.add("t1.txt", FileSystem.F1);
		_fs.add("test/t1.txt", FileSystem.F1);
		_fs.add("test/t2.txt", FileSystem.F2);
		_fs.add("test/t3.TXT", FileSystem.F2);
		_fs.add("test/t4.log", FileSystem.F2);
		_fs.add("test/t5.txt", FileSystem.F1);          // will be deleted
		_fs.add("test/t6.txt", FileSystem.F5);          // will be banned
		_fs.add("test1/t2.txt", FileSystem.F1);
		_fs.add("test1/t3.txt", FileSystem.F4);
		_fs.add("test1/folder1/t1.txt", FileSystem.F1);
		_fs.add("test2/a/1/t6.txt", FileSystem.F1);
// 		_fs.add("test2/a/t7.txt", FileSystem.F2);       // will be created later
		_fs.add("test2/a/t8.TXT", FileSystem.F3);
		_fs.add("test2/a/t9.txt", FileSystem.F5);       // will be banned
		_fs.create();
		
		executeRegisterFilesAction();
		checkCounts(_fs, 13, 13);
		_fs.checkAll(_scope.getConnection(), 0, 0);
				
		assertEquals(13, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(13, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(13, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(13, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		assertEquals(0, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(0, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));

		_fs.delete("test/t5.txt");
		Thread.sleep(1000);
		executeRegisterFilesAction();
		checkCounts(_fs, 12, 12);
		_fs.checkAll(_scope.getConnection(), 1, 1);
		
		assertEquals(13, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(12, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(13, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(12, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		assertEquals(0, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(0, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));
	}

	public void deduplicateFiles() throws Exception {
		registerFiles();
		
		// F1
		_fs.archiveAndKeep("test/t1.txt");
		_fs.archiveAndKeep("test1/t2.txt");
		_fs.archiveAndKeep("test1/folder1/t1.txt");
		_fs.archiveAndKeep("test2/a/1/t6.txt");

		// F2
		_fs.archiveAndKeep("test/t3.TXT");
		
		// F5
		_fs.archiveAndKeep("test2/a/t9.txt");
		
		executeDeduplicateAllFilesAction();
		_fs.checkAll(_scope.getConnection(), 1, 6+1);		
		
		long inode = file("t1.txt").getNodeId();
		assertEquals(inode, file("test/t1.txt").getNodeId());
		assertEquals(inode, file("test1/t2.txt").getNodeId());
		assertEquals(inode, file("test1/folder1/t1.txt").getNodeId());
		assertEquals(inode, file("test2/a/1/t6.txt").getNodeId());
		
		inode = file("test/t2.txt").getNodeId();
		assertEquals(inode, file("test/t3.TXT").getNodeId());		
		
		inode = file("test/t6.txt").getNodeId();
		assertEquals(inode, file("test2/a/t9.txt").getNodeId());		

		assertEquals(13, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(12, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(13, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(12, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		assertEquals(3, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(0, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));
		
	}

	public void banFiles() throws Exception {
		deduplicateFiles();
		
		_fs.add("test2/a/t7.txt", FileSystem.F2);		
		_fs.add("test2/t10.txt", FileSystem.F5);       // will be banned
		_fs.create();
		
		new RecordBannedFile(_scope, new File(_scope.getRootDirectory(), "test2/t10.txt"), null).save();
		
		_fs.softDelete("test/t6.txt");  
		_fs.softDelete("test2/a/t9.txt"); 
		_fs.softDelete("test2/t10.txt");   
		
		_scope.getInodeCache().reset();
		executeRegisterFilesAction();
		_fs.checkAll(_scope.getConnection(), 4, 9);

		assertEquals(15, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(11, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(15, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(11, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		// active multi-used (shared) inodes
		assertEquals(2, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(1, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));
	}
	
	public void createBaseTestData() throws Exception {
		banFiles();
	}
}
