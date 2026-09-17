package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import earth.cube.libs.testings.assertions.DateWrapper;
import earth.cube.tools.file_backup.commons.DatabaseDumper;
import earth.cube.tools.file_backup.commons.DatabaseNumberChecker;
import earth.cube.tools.file_backup.commons.DebugScope;
import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileName;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.model.test_files.CopiedTestFile;
import earth.cube.tools.file_backup.model.test_files.FileSystem;
import earth.cube.tools.file_backup.model.test_files.GeneratedFile;

public class RegisterFiles_BannedDirectoryTest extends AbstractActionTest {
	
	protected List<String> _deleteStructure = new ArrayList<>();
	protected int _nXattrFileCount;
	
	
	protected String addDeletion(String sPath) {
		_deleteStructure.add(sPath);
		return sPath;
	}
	
	protected void calcXattrFileCount() {
		_nXattrFileCount = (int) _deleteStructure.stream().filter( s -> {
			try {
				return FileCollection.isXattrSidecar(_fs.get(s).getFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} ).count();		
	}
	

	protected void createTestData(String sParentDir, String... saTriggers) throws Exception {
		createBaseTestData();
		
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/t1.txt"), FileSystem.F1);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/t2.txt"), FileSystem.F2);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/t2.txt"), FileSystem.F2);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/t4.log"), FileSystem.F4);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/t4.txt"), FileSystem.F3);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/t5.txt"), FileSystem.F6);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/t5.xmp"), FileSystem.F6);
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/t6.txt"), FileSystem.F5);  // ban candidate

//		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/._t1.txt"), new CopiedTestFile(new File("test-data/DeleteTrigger.xattr")));
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/._t2.txt"), new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/b/._t5.txt"), new CopiedTestFile(new File("test-data/conf.xattr")));
//		_fs.add(addDeletion(sParentDir + "/@DeleteDirectory/Navigon Map Update/a/._t2.txt"), new CopiedTestFile(new File("test-data/DeleteTrigger.xattr")));
		
		for(String sTrigger : saTriggers) {
			FileName name = new FileName(sParentDir + "/@DeleteDirectory/Navigon Map Update/" + sTrigger);
			name.setPrefix("._");
			_fs.add(addDeletion(name.getFile().toString()), new CopiedTestFile(new File("test-data/DeleteTrigger.xattr")));
		}
		
		_fs.add("test/a/t2.txt", FileSystem.F2);
		_fs.add("test/a/t2.xmp", FileSystem.F5);
		_fs.add("test/a/t3.txt", FileSystem.F3);
		_fs.add("test/a/t3.xmp", FileSystem.F6);
		_fs.add("test/a/b/t4.log", FileSystem.F4);
		_fs.add("test/a/b/t4.txt", FileSystem.F3);
		_fs.add("test/a/b/t5.txt", FileSystem.F6);
		_fs.add("test/a/b/t5.xmp", FileSystem.F6);
				
		_fs.add("test3/_/t1.txt", FileSystem.F1);
		_fs.add("test3/_/t2.txt", FileSystem.F1);		
		_fs.add("test3/_/._t2.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test3/_/a/t2.txt", FileSystem.F2);
		_fs.add("test3/_/a/t6.txt", FileSystem.F3);
		_fs.add("test3/_/a/b/t4.log", FileSystem.F4);
		_fs.add("test3/_/a/b/t4.txt", FileSystem.F4);
		_fs.add("test3/_/a/b/t5.txt", FileSystem.F6);
		_fs.add("test3/_/a/b/t5.xmp", FileSystem.F6);
		
		_fs.add("test4/t1.txt", FileSystem.F1);
		_fs.add("test4/t2.txt", FileSystem.F2);		
		_fs.add("test4/a/t3.txt", FileSystem.F3);
		_fs.add("test4/a/t3.xmp", FileSystem.F6);
		_fs.add("test4/a/t6.txt", FileSystem.F3);
		_fs.add("test4/a/b/t4.log", FileSystem.F3);
		_fs.add("test4/a/b/t4.txt", FileSystem.F3);
		_fs.add("test4/a/b/t5.xmp", FileSystem.F6);
		
		_fs.add("test5/t1.txt", FileSystem.F1);
		_fs.add("test5/t2.txt", FileSystem.F2);
		_fs.add("test5/._t2.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test5/a/t2.txt", FileSystem.F2);		
		_fs.add("test5/a/b/t4.log", FileSystem.F4);
		_fs.add("test5/a/b/t4.txt", FileSystem.F3);
		_fs.add("test5/a/b/t5.txt", FileSystem.F6);
		_fs.add("test5/a/b/t5.xmp", FileSystem.F6);
		_fs.add("test5/a/b/t6.txt", FileSystem.F5);

		_fs.create();
		
	}
	
	
	/**
	 * Only to get the test data dumped for verification / JUnit code development.
	 * @throws Exception
	 */
	@Test
	public void test_dump_data_1() throws Exception {
		createTestData("a");
		calcXattrFileCount();
				
		System.out.println(_fs.getDump());

		/* Output:
		
			/a/@DeleteDirectory/Navigon Map Update/._t2.txt      conf.xattr
			/a/@DeleteDirectory/Navigon Map Update/t1.txt        F1
			/a/@DeleteDirectory/Navigon Map Update/t2.txt        F2
			/a/@DeleteDirectory/Navigon Map Update/a/t2.txt      F2
			/a/@DeleteDirectory/Navigon Map Update/a/b/._t5.txt  conf.xattr
			/a/@DeleteDirectory/Navigon Map Update/a/b/t4.log    F4
			/a/@DeleteDirectory/Navigon Map Update/a/b/t4.txt    F3
			/a/@DeleteDirectory/Navigon Map Update/a/b/t5.txt    F6
			/a/@DeleteDirectory/Navigon Map Update/a/b/t5.xmp    F6
			/a/@DeleteDirectory/Navigon Map Update/a/b/t6.txt    F5
			
			/t1.txt                                              F1
			
			/test/t1.txt                                         F1
			/test/t2.txt                                         F2
			/test/t3.TXT                                         F2
			/test/t4.log                                         F2
			/test/a/t2.txt                                       F2
			/test/a/t2.xmp                                       F5
			/test/a/t3.txt                                       F3
			/test/a/t3.xmp                                       F6
			/test/a/b/t4.log                                     F4
			/test/a/b/t4.txt                                     F3
			/test/a/b/t5.txt                                     F6
			/test/a/b/t5.xmp                                     F6
			
			/test1/t2.txt                                        F1
			/test1/t3.txt                                        F4
			/test1/folder1/t1.txt                                F1
			
			/test2/a/t7.txt                                      F2
			/test2/a/t8.TXT                                      F3
			/test2/a/1/t6.txt                                    F1
			
			/test3/_/t1.txt                                      F1
			/test3/_/t2.txt                                      F2
			/test3/_/a/t2.txt                                    F2
			/test3/_/a/t6.txt                                    F3
			/test3/_/a/b/t4.log                                  F4
			/test3/_/a/b/t4.txt                                  F4
			/test3/_/a/b/t5.txt                                  F6
			/test3/_/a/b/t5.xmp                                  F6
			
			/test4/t1.txt                                        F1
			/test4/t2.txt                                        F2
			/test4/a/t3.txt                                      F3
			/test4/a/t3.xmp                                      F6
			/test4/a/t6.txt                                      F3
			/test4/a/b/t4.log                                    F3
			/test4/a/b/t4.txt                                    F3
			/test4/a/b/t5.xmp                                    F6
			
			/test5/t1.txt                                        F1
			/test5/t2.txt                                        F2
			/test5/._t2.txt                                      conf.xattr
			/test5/a/t2.txt                                      F2
			/test5/a/b/t4.log                                    F4
			/test5/a/b/t4.txt                                    F3
			/test5/a/b/t5.txt                                    F6
			/test5/a/b/t5.xmp                                    F6
			/test5/a/b/t6.txt                                    F5 
		 
		 */
		
	}
	
	@Test
	public void test_1() throws Exception {		
		createTestData("a");
		calcXattrFileCount();
		
		assertEquals(10, _deleteStructure.size());

		_fs.softDelete("test5/a/b/t6.txt");
		
		executeRegisterFilesAction();
		
		DatabaseDumper.from(_scope).dumpCompoundFiles();

		DatabaseDumper.from(_scope).query("select * from file").execute();

		_fs.checkAll(_scope.getConnection(), 5, 10);
		assertEquals(5, _fs.getSoftDeletionCount() + _fs.getHardDeletionCount() + _fs.getArchiveRemovalCount());
		assertEquals(10, _fs.getOrphanedInodeCount());

		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(2).banned(1)
			.bannedDirectoryEntries(0).bannedDirectoryTriggers(0).check();
	}
	
	
	@Test
	public void test_2() throws Exception {	
		createTestData("a", "t1.txt", "a/t2.txt");
		calcXattrFileCount();
		
		assertEquals(12, _deleteStructure.size());
		assertEquals(4, _nXattrFileCount);
		
		_deleteStructure.stream().forEach( s -> _fs.softDelete(s) );
				
		_fs.create("test/purged.txt", new GeneratedFile("purged.txt"));
		_fs.softDelete("test/t1.txt", "test (1)/t1.txt");
		_fs.softDelete("test/t2.txt", "test (1)/t2.txt");
		_fs.softDelete("test/a/t2.txt", "test (1)/a/t2.txt");
		_fs.softDelete("test/a/t2.xmp", "test (1)/a/t2.xmp");
		_fs.softDelete("test/a/b/t4.log", "test (1)/a/b/t4.log");
		_fs.softDelete("test/a/b/t4.txt", "test (1)/a/b/t4.txt");
		_fs.softDelete("test/a/b/t5.txt", "test (1)/a/b/t5.txt");
		_fs.softDelete("test/a/b/t5.xmp", "test (1)/a/b/t5.xmp");

		_fs.create("test3/_/purged.txt", new GeneratedFile("purged.txt"));
		_fs.softDelete("test3/_/t1.txt");
		_fs.softDelete("test3/_/a/t2.txt");
		_fs.softDelete("test3/_/a/b/t4.log");
		_fs.softDelete("test3/_/a/b/t5.txt");
		_fs.softDelete("test3/_/a/b/t5.xmp");
		
		_fs.create("test5/purged.txt", new GeneratedFile("purged.txt"));
		_fs.softDelete("test5/t1.txt");
		_fs.softDelete("test5/t2.txt");
		_fs.softDelete("test5/a/t2.txt");
		_fs.softDelete("test5/._t2.txt");
		_fs.softDelete("test5/a/b/t4.log");
		_fs.softDelete("test5/a/b/t4.txt");
		_fs.softDelete("test5/a/b/t5.txt");
		_fs.softDelete("test5/a/b/t5.xmp");
		_fs.softDelete("test5/a/b/t6.txt");
	
		
		DatabaseDumper.from(_scope).dumpCompoundFiles();

		System.out.println("1: " + new DateWrapper(_fs.get("test/t3.TXT").getLinuxFile().getInode().getChangedTime()).toString());
		
		executeRegisterFilesAction();
		
		System.out.println("2: " + new DateWrapper(_fs.get("test/t3.TXT").getLinuxFile().getInode().getChangedTime()).toString());
		
		DatabaseDumper.from(_scope).dumpCompoundFiles();
		
		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(1).banned(1)
			.bannedDirectoryEntries(_deleteStructure.size() - _nXattrFileCount)  // valid xattr files are not registered
			.bannedDirectoryTriggers(2).check();
		
		assertTrue(new File(_scope.getRootDirectory(), "test5").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test5/a").exists());
	}	
	

	@Test
	public void test_3() throws Exception {
		createTestData("a", "t1.txt", "a/b/t6.txt");
		calcXattrFileCount();

		assertEquals(12, _deleteStructure.size());
		
		_deleteStructure.stream().forEach( s -> _fs.softDelete(s) );
						
		_fs.create("test5/purged.txt", new GeneratedFile("purged.txt"));
		_fs.softDelete("test5/t1.txt");
		_fs.softDelete("test5/t2.txt");
		_fs.softDelete("test5/a/t2.txt");
		_fs.softDelete("test5/._t2.txt");
		_fs.softDelete("test5/a/b/t4.log");
		_fs.softDelete("test5/a/b/t4.txt");
		_fs.softDelete("test5/a/b/t5.txt");
		_fs.softDelete("test5/a/b/t5.xmp");
		_fs.softDelete("test5/a/b/t6.txt");
	
		executeRegisterFilesAction();
		
		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(2).banned(1)
			.bannedDirectoryEntries(_deleteStructure.size() - _nXattrFileCount)
			.bannedDirectoryTriggers(2).check();
		
		assertTrue(new File(_scope.getRootDirectory(), "test5").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test5/a").exists());
	
	}
	
}
