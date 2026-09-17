package earth.cube.tools.zip_web_browser.flattened_directory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlatDirectory extends FlatEntry {

	protected List<FlatDirectory> _directories = new ArrayList<>();
	
	protected List<FlatEntry> _files = new ArrayList<>();
	
	{
		_bDirectory = true;
	}
	
	protected FlatDirectory() {
	}

	public FlatDirectory(File path) {
		super(path);
	}
	
	public List<FlatDirectory> getDirectories() {
		return Collections.unmodifiableList(_directories);
	}
	
	public List<FlatEntry> getFiles() {
		return Collections.unmodifiableList(_files);
	}
	
	public void addDirectory(FlatDirectory dir) {
		String s = dir.getName();
		int n = _directories.size();
		for(int i = 0; i < n; i++) {
			if(s.compareToIgnoreCase(_directories.get(i).getName()) < 0) {
				_directories.add(i, dir);
				return;
			}
		}
		_directories.add(dir);
	}
		
	public void addFile(FlatEntry file) {
		String s = file.getName();
		int n = _files.size();
		for(int i = 0; i < n; i++) {
			if(s.compareToIgnoreCase(_files.get(i).getName()) < 0) {
				_files.add(i, file);
				return;
			}
		}
		_files.add(file);
	}

}
