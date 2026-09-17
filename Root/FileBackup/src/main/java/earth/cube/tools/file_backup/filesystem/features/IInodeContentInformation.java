package earth.cube.tools.file_backup.filesystem.features;

import earth.cube.tools.file_backup.files.common.IContentKey;
import earth.cube.tools.file_backup.filesystem.common.InodeInfo;

public interface IInodeContentInformation extends InodeInfo, IContentKey {
	
	boolean isChecksumVerified();
}
