package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ShowDuplicatesActionTest extends AbstractActionTest {

	
/*		
	_fs.add("/t1.txt", FileSystem.F1);
	_fs.add("/test/t1.txt", FileSystem.F1);
	_fs.add("/test/t2.txt", FileSystem.F2);
	_fs.add("/test/t3.TXT", FileSystem.F2);
	_fs.add("/test/t4.log", FileSystem.F2);
	_fs.add("/test1/t2.txt", FileSystem.F1);
	_fs.add("/test1/t3.txt", FileSystem.F4);
	_fs.add("/test1/folder1/t1.txt", FileSystem.F1);
	_fs.add("/test2/a/1/t6.txt", FileSystem.F1);
		_fs.add("/test2/a/t7.txt", FileSystem.F2);       // will be created later
	_fs.add("/test2/a/t8.TXT", FileSystem.F3);
*/			
	
	@Test
	public void test_1() throws Exception {
		createBaseTestData();
		executeShowDuplicatesAction("test");
		System.out.println(_sOut);
		
		String sExpected = ""
			+ "test/t1.txt\n"
			+ "    t1.txt\n"
			+ "    test1/folder1/t1.txt\n"
			+ "    test1/t2.txt\n"
			+ "    test2/a/1/t6.txt\n"
			+ "test/t2.txt\n"
			+ "    test/t3.TXT\n"
			+ "    test2/a/t7.txt\n"
			+ "test/t3.TXT\n"
			+ "    test/t2.txt\n"
			+ "    test2/a/t7.txt\n"
			+ "test/t4.log\n";
		
		assertEquals(sExpected, _sOut);
	}

}
