package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.commons.DatabaseNumberChecker;
import earth.cube.tools.file_backup.commons.Query;

public class DeduplicateFilesActionTest extends AbstractActionTest {
	
	
	
	
	@Test
	public void test_1() throws Exception {
		deduplicateFiles();
	}
	
	@Test
	public void test_2() throws Exception {
		banFiles();
				
		// F2
		_fs.archiveAndKeep("test2/a/t7.txt");
		_fs.get("test2/a/t7.txt").setDate(_fs.get("test/t2.txt").getDate()); // Hardlink did reset last modified date
		
		executeDeduplicateAllFilesAction();
		_fs.checkAll(_scope.getConnection(), 4, 10);		
		
		long inode = file("t1.txt").getNodeId();
		assertEquals(inode, file("test/t1.txt").getNodeId());
		assertEquals(inode, file("test1/t2.txt").getNodeId());
		assertEquals(inode, file("test1/folder1/t1.txt").getNodeId());
		assertEquals(inode, file("test2/a/1/t6.txt").getNodeId());
		
		inode = file("test/t2.txt").getNodeId();
		assertEquals(inode, file("test/t3.TXT").getNodeId());		
		assertEquals(inode, file("test2/a/t7.txt").getNodeId());		
		
		assertEquals(15, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file").execute().asInt("cnt")));
		assertEquals(11, Query.perform(_scope.getConnection(), 
				query -> query.query("select count(*) as cnt from file where valid=1").execute().asInt("cnt")));

		assertEquals(15, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from inode").execute().asInt("cnt")));
		
		assertEquals(11, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from file f, inode n where f.inode=n.id").execute().asInt("cnt")));

		assertEquals(2, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from (select f.inode, count(*) from file f, inode n where f.inode=n.id group by f.inode having count(*) > 1)").execute().asInt("cnt")));
		
		assertEquals(1, Query.perform(_scope.getConnection(),
				query -> query.query("select count(*) as cnt from banned_file").execute().asInt("cnt")));
		
		// same as above 6 assertions but this is not redundant as the following backed/tested by the above assertions
		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(2).banned(1).check();
	}
	

	@Test
	public void test_no_deduplication_1() throws Exception {
		banFiles();
				
		// /test2/a/t7.txt (F2) will not be deduplicated as file limit is not hit
		
		executeDeduplicateMostFilesAction();
		
		long inode = file("t1.txt").getNodeId();
		assertEquals(inode, file("test/t1.txt").getNodeId());
		assertEquals(inode, file("test1/t2.txt").getNodeId());
		assertEquals(inode, file("test1/folder1/t1.txt").getNodeId());
		assertEquals(inode, file("test2/a/1/t6.txt").getNodeId());
		
		inode = file("test/t2.txt").getNodeId();
		assertEquals(inode, file("test/t3.TXT").getNodeId());		
		assertNotSame(inode, file("test2/a/t7.txt").getNodeId());		
		
		_fs.checkAll(_scope.getConnection(), 4, 9);

		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(2).banned(1).check();
	}
	
}
