package earth.cube.tools.file_backup.commons;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.actions.AbstractActionTest;

public class DatabaseDumperTest extends AbstractActionTest {
	
	@Test
	public void test_1() throws Exception {
		createBaseTestData();
		
		DatabaseDumper.from(_scope).dumpCompoundFiles();
	}

}
