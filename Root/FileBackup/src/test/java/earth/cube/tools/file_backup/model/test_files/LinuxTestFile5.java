package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class LinuxTestFile5 extends AbstractLinuxTestFile {

	public LinuxTestFile5() {
		super("F5", "This is a the content of file 5!");
		
		/**
		 * echo -n 'This is a the content of file 5!' | shasum -a 256
		 */
		assertEquals("a6b57d39d6f9a867c44a05e26810f3c4af392d01687c7acc5dddf244557d4062", _sSha256);
	}	
	
	
}
