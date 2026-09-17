package earth.cube.tools.file_backup.filesystem.features;

import earth.cube.tools.file_backup.files.common.IContentKey;

public interface IFileInformation extends IContentKey {
	
	String getPath();
	
	boolean isValid();

}
