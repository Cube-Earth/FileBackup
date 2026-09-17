package earth.cube.tools.file_backup.files.common;

import java.util.Date;

public interface IInodeKey {

	long getInodeId();
	
	Date getInodeChangedTime();
	
}
