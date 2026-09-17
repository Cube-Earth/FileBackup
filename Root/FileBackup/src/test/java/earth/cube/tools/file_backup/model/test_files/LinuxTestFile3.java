package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class LinuxTestFile3 extends AbstractLinuxTestFile {
	
	public LinuxTestFile3() {
		super("F3", "This is a the content of file 3!");
		
		/**
		 * echo -n 'This is a the content of file 3!' | shasum -a 256
		 */
		assertEquals("3512b6c99c99d68b04d8676bcbe1e0f30234383825a5b534049d3bce596c35f3", _sSha256);
	}	
	
	
}
