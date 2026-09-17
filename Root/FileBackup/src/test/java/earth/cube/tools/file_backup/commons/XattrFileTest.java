package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class XattrFileTest {
	
	@Test
	public void test_1() throws IOException {
		XattrFile f = new XattrFile(new File("test-data/conf.xattr"));
		
		assertTrue(f.isValid());

		assertTrue(f.hasTag("Test"));
		assertTrue(f.hasLoweredTag("test"));
		assertFalse(f.hasTag("test"));
		
		assertTrue(f.hasTag("Lila"));
		assertEquals(2, f.getAllTags().size());
		
	}

}
