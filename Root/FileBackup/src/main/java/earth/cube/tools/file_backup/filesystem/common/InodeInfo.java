package earth.cube.tools.file_backup.filesystem.common;

import java.util.Date;

public interface InodeInfo {
	
	long getInodeId();
	
	Date getInodeChangedTime();

}
