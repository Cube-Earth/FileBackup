package earth.cube.tools.file_backup.model.test_files;

import java.io.File;
import java.io.IOException;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;

public class CopiedTestFile extends AbstractLinuxTestFile {
	
	
	public CopiedTestFile(String sLogicalName, File srcFile) throws IOException {
		super(sLogicalName, FileUtil.read(srcFile));
	}

	public CopiedTestFile(File srcFile) throws IOException {
		super(srcFile.getName(), FileUtil.read(srcFile));
	}

	public CopiedTestFile(String sLogicalName, byte[] content) throws IOException {
		super(sLogicalName, content);
	}

	
	public AbstractLinuxTestFile copy() {
		try {
			return new CopiedTestFile(_sLogicalName, _content.clone());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
