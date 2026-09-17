package earth.cube.tools.file_backup.actions;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.commons.AssertUtils;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.ValidationError;
import earth.cube.tools.file_backup.model.test_files.FileSystem;

public class RegisterFilesInDatabaseActionTest extends AbstractActionTest {

	
	@Test
	public void test_1() throws Exception {
		FileSystem fs = new FileSystem(_rootDir);
		fs.add("test/t1.txt", FileSystem.F1);
		fs.add("test/t2.txt", FileSystem.F2);
		fs.add("test1/t1.txt", FileSystem.F3);
		fs.add("test1/t2.txt", FileSystem.F1);
		fs.create();
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		
		// database is still empty
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		
		executeRegisterFilesAction();
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0);
		fs.checkAll(_scope.getConnection(), 0, 0);
		
		Query.perform(_scope.getConnection(), q -> q.query("delete from file where path='test/t1.txt'").execute());
		
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		fs.checkAgainstPhysicalFiles();
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		
	}

	@Test
	public void test_2() throws Exception {
		FileSystem fs = new FileSystem(_rootDir);
		fs.add("test/t1.txt", FileSystem.F1);
		fs.add("test/t2.txt", FileSystem.F2);
		fs.add("test1/t1.txt", FileSystem.F3);
		fs.add("test1/t2.txt", FileSystem.F1);
		fs.create();
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		
		// database is still empty
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		
		executeRegisterFilesAction();
		
		checkCounts(fs, 4, 4);

		fs.checkAll(_scope.getConnection(), 0, 0);
		
		fs.moveOut("test/t1.txt");
		AssertUtils.assertValidationThrows(ValidationError.EXPECTED_FILE_VALID, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		fs.checkAgainstPhysicalFiles();
		AssertUtils.assertValidationThrows(ValidationError.EXPECTED_FILE_VALID, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		
		Thread.sleep(1000);
		fs.moveIn("test/t1.txt");
		
		AssertUtils.assertValidationThrows(ValidationError.CHANGED_TIME_NEWER, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		fs.checkAgainstPhysicalFiles();
		AssertUtils.assertValidationThrows(ValidationError.CHANGED_TIME_NEWER, () -> fs.checkAll(_scope.getConnection(), 0, 0));
	}

	@Test
	public void test_3() throws Exception {
		FileSystem fs = new FileSystem(_rootDir);
		fs.add("test/t1.txt", FileSystem.F1);
		fs.add("test/t2.txt", FileSystem.F2);
		fs.add("test1/t1.txt", FileSystem.F3);
		fs.add("test1/t2.txt", FileSystem.F1);
		fs.create();
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		
		// database is still empty
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		
		executeRegisterFilesAction();
		
		checkCounts(fs, 4, 4);

		fs.checkAll(_scope.getConnection(), 0, 0);
		
		fs.delete("test/t1.txt");
		checkCounts(fs, 3, 3);
		AssertUtils.assertValidationThrows(ValidationError.EXPECTED_FILE_VALID, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		fs.checkAgainstPhysicalFiles();
		AssertUtils.assertValidationThrows(ValidationError.EXPECTED_FILE_VALID, () -> fs.checkAll(_scope.getConnection(), 0, 0));

		Thread.sleep(1000);
		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);
		
		fs.create("test2/t1.pdf", FileSystem.F2);
		checkCounts(fs, 4, 4);
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkAll(_scope.getConnection(), 0, 0));

		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);

		fs.update("test1/t1.txt", FileSystem.F1, true);
		checkCounts(fs, 4, 4);
		AssertUtils.assertValidationThrows(ValidationError.CHANGED_TIME_NEWER, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);

		Thread.sleep(1005 - System.currentTimeMillis() % 1000); // force that all next operations are within the same second, so that an SHA256 error is raised and the CHANGED_TIME is avoided

		fs.update("test1/t1.txt", FileSystem.F1, false);
		checkCounts(fs, 4, 4);
		AssertUtils.assertValidationThrows(ValidationError.MODIFICATION_TIME_NEWER, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);

		fs.update("test1/t1.txt", FileSystem.F2, false);
		checkCounts(fs, 4, 4);
		AssertUtils.assertValidationThrows(ValidationError.SHA256, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);	
	}

	@Test
	public void test_4() throws Exception {
		Thread.sleep(1005 - System.currentTimeMillis() % 1000); // force that all next operations are within the same second, so that an SHA256 error is raised and the CHANGED_TIME is avoided

		FileSystem fs = new FileSystem(_rootDir);
		fs.add("test/t1.txt", FileSystem.F1);
		fs.add("test/t2.txt", FileSystem.F2);
		fs.add("test1/t1.txt", FileSystem.F3);
		fs.add("test1/t2.txt", FileSystem.F1);
		fs.create();
		
		executeRegisterFilesAction();
				
		fs.delete("test/t1.txt");
		fs.create("test2/t1.pdf", FileSystem.F2);
		fs.update("test1/t1.txt", FileSystem.F1, true);
		fs.update("test/t2.txt", FileSystem.F3, true);
		fs.create("test2/t3.pdf", FileSystem.F3);
		checkCounts(fs, 5, 5);
		AssertUtils.assertValidationThrows(ValidationError.SHA256, () -> fs.checkAll(_scope.getConnection(), 0, 0));

		Thread.sleep(1000);
		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 1, 1);	

		fs.create("test/t1.txt", FileSystem.F1);
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkAll(_scope.getConnection(), 1, 1));

		executeRegisterFilesAction();
		fs.checkAll(_scope.getConnection(), 0, 1);	
	}
	
	@Test
	public void test_5() throws Exception {
		registerFiles();
	}
	
}
