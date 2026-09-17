package earth.cube.tools.file_backup.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.files.FileSystem;
import lombok.Getter;
import lombok.Setter;

public class Directory<T> {
	
	@Getter
	protected File _file;
	
	@Getter
	protected boolean _bValid;
	
	@Setter
	protected T _context;

	@Setter
	protected BiConsumer<T, CompoundFile> _fileProcessing;
	
	@Setter
	protected BiConsumer<T, File> _bannedDirectoryProcessing;
	
	@Setter
	protected BiConsumer<T, File> _bannedFileProcessing;

	protected FileSystem _fs;

	
	public Directory(FileSystem fs, File file) {
		_fs = fs;
		_file = file;
		_bValid = !Globals.isProtected(_file);
	}
	
	
	public void visitDeep() throws IOException {
		visit(_file);
	}


	private void visit(File dir) throws IOException {
		DirectoryAnalyzer analyzer = new DirectoryAnalyzer(_fs, dir);
		if(!analyzer.isValid())
			return;
		
		if(dir.getName().equals(Globals.BANNED_DIRECTORY_TOKEN)) {
			for(File subDir : analyzer.getDirectories()) {
				_bannedDirectoryProcessing.accept(_context, subDir);
			}
		}
		else 
			if(dir.getName().equals(Globals.BANNED_FILE_TOKEN)) {
				_bannedDirectoryProcessing.accept(_context, dir);
			}
			else {
				for(CompoundFile file : analyzer.getFiles()) {
					_fileProcessing.accept(_context, file);
				}
				
				for(File subDir : analyzer.getDirectories()) {
					visit(subDir);
				}
			}
	}

}
