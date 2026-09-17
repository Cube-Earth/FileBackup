package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class LinuxTestFile4 extends AbstractLinuxTestFile {
	
	public LinuxTestFile4() {
		super("F4", "This is a the content of file 4!");
		
		/**
		 * echo -n 'This is a the content of file 4!' | shasum -a 256
		 */
		assertEquals("d9d80d8bbe06f55127e9068a8e0bd91b019d4658c272905604a42dbc0a53b5b2", _sSha256);
	}	

}
