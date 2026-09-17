package earth.cube.tools.file_backup.file_filter;

import java.io.File;

public interface IFilePattern {

	FileMatched matches(File file, boolean bNested);

}
