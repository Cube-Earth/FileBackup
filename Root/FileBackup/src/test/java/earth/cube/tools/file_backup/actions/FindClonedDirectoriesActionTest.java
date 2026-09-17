package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FindClonedDirectoriesActionTest extends AbstractActionTest {

	
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
		executeFindClonedDirectoriesAction("test");
		System.out.println(_sOut);
		
		String sExpected = ""
				+ "66.67    2/3 (4)   test2\n"
				+ "66.67    2/3 (4)   test2/a\n"
				+ "33.33    1/3 (4)   test1\n"
				+ "33.33    1/3 (4)   test1/folder1\n"
				+ "33.33    1/3 (4)   test2/a/1\n";
		
		assertEquals(sExpected, _sOut);
	}
	
	@Test
	public void test_2() throws Exception {
		createBaseTestData();
		executeFindClonedDirectoriesAction("test2/a/1");
		System.out.println(_sOut);
		
		String sExpected = ""
				+ "100.00    1/1 (1)   test\n"
				+ "100.00    1/1 (1)   test1\n"
				+ "100.00    1/1 (1)   test1/folder1\n";
		
		assertEquals(sExpected, _sOut);
	}

}
