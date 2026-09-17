package earth.cube.tools.file_backup.files;

import java.io.File;

import earth.cube.tools.file_backup.model.Directory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BasicFile {
	
	@Getter
	private Directory _directory;
	
	@Getter
	private File _file;
	
	

}
