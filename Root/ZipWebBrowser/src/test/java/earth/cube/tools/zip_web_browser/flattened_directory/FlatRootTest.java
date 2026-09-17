package earth.cube.tools.zip_web_browser.flattened_directory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class FlatRootTest {

	@Test
	public void test_1() {
		FlatRoot root = new FlatRoot();
		FlatDirectory dir;
		FlatEntry file;
		
		root.addFile("/abc/def/ghi.txt").init( e -> e.setData("f1") );
		root.addDirectory("/abc/jkl").init( e -> e.setData("d1") );
		root.addDirectory("/abc/add").init( e -> e.setData("d2") );
		root.addDirectory("/abc/add").init( e -> e.setData("d3") );
		root.addDirectory("/abc").init( e -> e.setData("d4") );
		root.addFile("/abc/add/pqr.pdf").init( e -> e.setData("f2") );
		root.addFile("/abc/add/stu.pdf").init( e -> e.setData("f3") );
		
		assertEquals(8, root._entries.size());
		
		
		//{{-- ""
		dir = root.getEntry(FlatRoot.ROOT);
		assertNull(dir.getData());
		assertEquals(1, dir._directories.size());
		assertEquals("abc", dir._directories.get(0).getName());
		assertEquals("d4", dir._directories.get(0).getData());
		
		assertEquals(0, dir._files.size());
		//--}}

		//{{-- /abc
		dir = root.getEntry("/abc");
		assertEquals("d4", dir.getData());
		assertEquals(3, dir._directories.size());
		assertEquals("add", dir._directories.get(0).getName());
		assertEquals("d2", dir._directories.get(0).getData());
		assertEquals("def", dir._directories.get(1).getName());
		assertEquals("jkl", dir._directories.get(2).getName());
		
		assertEquals(0, dir._files.size());
		//--}}
		
		//{{-- /abc/def
		dir = root.getEntry("/abc/def");
		assertEquals(null, (String) dir.getData());
		assertEquals("def", dir.getName());
		assertEquals("/abc/def", dir.getPath());
		assertEquals(0, dir._directories.size());
		
		assertEquals(1, dir._files.size());
		assertEquals("ghi.txt", dir._files.get(0).getName());
		assertEquals("/abc/def/ghi.txt", dir._files.get(0).getPath());
		assertEquals("f1", dir._files.get(0).getData());
		//--}}

		//{{-- /abc/jkl
		dir = root.getEntry("/abc/jkl");
		assertEquals("d1", (String) dir.getData());
		assertEquals(0, dir._directories.size());
		assertEquals(0, dir._files.size());
		//--}}

		//{{-- /abc/add
		dir = root.getEntry("/abc/add");
		assertEquals("d2", (String) dir.getData());
		assertEquals(0, dir._directories.size());
		assertEquals(2, dir._files.size());
		assertEquals("pqr.pdf", dir._files.get(0).getName());
		assertEquals("f2", dir._files.get(0).getData());
		assertEquals("stu.pdf", dir._files.get(1).getName());
		assertEquals("f3", dir._files.get(1).getData());
		//--}}

		//{{-- /abc/xyz
		dir = root.getEntry("/abc/xyz");
		assertNull(dir);
		//--}}

		//{{-- /abc/xyz.txt
		file = root.getEntry("/abc/xyz.txt");
		assertNull(dir);
		//--}}
		
		// TODO: continue implemenatation
	}
	
}
