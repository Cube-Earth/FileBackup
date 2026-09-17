package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class LinuxTestFile2 extends AbstractLinuxTestFile {

	
	public LinuxTestFile2() {
		super("F2", "This is a the content of file 2!");
		
		/**
		 * echo -n 'This is a the content of file 2!' | shasum -a 256
		 */
		assertEquals("49f2215acf2e558fa704dcc25a1b3edfe8d99f2ed68b526fc40f8a8cf1c7fd64", _sSha256);
	}
	
}
