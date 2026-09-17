package earth.cube.tools.file_backup.file_filter;

import java.io.File;

public class NotMatchedPattern implements IFilePattern {

	@Override
	public FileMatched matches(File file, boolean bNested) {
		return FileMatched.NOT_MATCHED;
	}

}
