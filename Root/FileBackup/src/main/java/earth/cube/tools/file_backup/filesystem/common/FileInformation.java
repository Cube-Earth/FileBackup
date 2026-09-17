package earth.cube.tools.file_backup.filesystem.common;

import earth.cube.tools.file_backup.files.common.ContentKey;
import earth.cube.tools.file_backup.filesystem.features.IFileInformation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
public class FileInformation extends ContentKey implements IFileInformation {
	
	@Getter
	protected String _sPath;
	
	@Getter
	protected boolean _bValid;
	
}
