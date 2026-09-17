package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import earth.cube.tools.file_backup.commons.IFuncBiFunction;
import earth.cube.tools.file_backup.commons.IFuncFunction;
import lombok.Getter;

public class Directory {
	
	@Getter
	private File _baseDirectory;
	
	
	private Set<File> _files = new HashSet<>();
	
	private Set<File> _directories = new HashSet<>();

	private Set<File> _processedFiles = new HashSet<>();
	
	private boolean _bFullRead;

	
	public Directory(File baseDir) {
		_baseDirectory = baseDir;
	}
	
	
	protected void readFull() {
		if(_bFullRead)
			return;
		
		for(File file : _baseDirectory.listFiles()) {
			if(file.isDirectory())
				_directories.add(file);
			else
				_files.add(file);
		}
	}

	
	public File processFile(String sName, IFuncBiFunction<Directory, File, Boolean> func) throws IOException, SQLException {
		File file = new File(_baseDirectory, sName);
		if(_files.contains(file)) {
			if(_processedFiles.contains(file))
				throw new IllegalStateException("Hu? Already processed! " + file.getAbsolutePath());
		}
		else {
			if(_bFullRead || !file.exists() || file.isDirectory())
				return null;
			_files.add(file);
		}
		if(func.apply(this, file)) {
			_processedFiles.add(file);
			return file;
		}
		else
			return null;
	}
	
	
	public Directory processUnprocessedFiles(IFuncBiFunction<Directory, File, Boolean> func) throws IOException, SQLException {
		readFull();
		Set<File> unprocessed = new HashSet<>(_files);
		unprocessed.removeAll(_processedFiles);
		for(File file : unprocessed) {
			func.consume(this, file);
		}
		return this;
	}
	
	public Directory processFiles(IFuncFunction<File, Boolean> func) throws IOException, SQLException {
		readFull();
		for(File file : _files) {
			if(func.apply(file))
				_processedFiles.add(file);
		}
		return this;
	}
	
	

}
