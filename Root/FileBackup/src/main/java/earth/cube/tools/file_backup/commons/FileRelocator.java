package earth.cube.tools.file_backup.commons;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor
public class FileRelocator {
	
	@Getter @Setter
	protected File _sourceDirecory;
	
	@Getter @Setter
	protected File _destinationDirectory;
	
	@Getter @Setter
	protected boolean _bCreateDirectories;

	
	public File relocate(File file) {
		return FileUtil.relocate(_sourceDirecory, _destinationDirectory, file, _bCreateDirectories);
	}
	
	public String getRelativePath(File file) {
		return FileUtil.getRelativePath(_sourceDirecory, file);
	}
}
