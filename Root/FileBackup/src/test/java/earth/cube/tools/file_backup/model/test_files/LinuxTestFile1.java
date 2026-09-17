package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class LinuxTestFile1 extends AbstractLinuxTestFile {
	
	public LinuxTestFile1() {
		super("F1", "This is a the content of file 1!");
		
		/**
		 * echo -n 'This is a the content of file 1!' | shasum -a 256
		 */
		assertEquals("1eb07029742ae554bb77eb6ab9f18acb4c69065932a8e2f30852cf785786fb3a", _sSha256);
	}
		
}
