package earth.cube.tools.file_backup.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.commons.ExpressionHelper;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.files.FileSystem;
import lombok.Getter;

public class DirectoryAnalyzer {
	
	@Getter
	protected File _baseDirectory;
	
	@Getter
	protected List<File> _directories;
	
	@Getter
	protected List<File> _orphanedFiles;
	
	@Getter
	protected List<CompoundFile> _files;
	
	@Getter
	protected Map<String,List<CompoundFile>> _groups;

	@Getter
	protected boolean _bValid;

	protected FileSystem _fs;
	
	
	public DirectoryAnalyzer(FileSystem fs, File baseDir) throws IOException {
		_fs = fs;
		_baseDirectory = baseDir;
		_bValid = !Globals.isProtected(_baseDirectory);
		if(_bValid) {
			analyze();
		}
	}
	
	
	private void analyze() throws IOException {
		File[] files = _baseDirectory.listFiles();
		
		List<File> dirs = new ArrayList<>();
		
		Map<String,CompoundFile> fileMap = new HashMap<>();
		
		List<File> orphanedFiles = new ArrayList<>();

		Map<String,List<CompoundFile>> groups = new HashMap<>();
		
		boolean bValid;
		for(File file : files) {
			if(file.isDirectory()) {
				bValid = !Globals.isProtected(file);
				if(bValid) {
					dirs.add(file);
				}
			}
			else {
				if(!Globals.isProtected(file)) {
					if(file.getName().equals(".DS_Store")) {
						orphanedFiles.add(file);
					}
					else {
						CompoundFile compoundFile = _fs.getFile(file);
						if(compoundFile.isValid()) {
							fileMap.put(file.getName(), compoundFile);
						}
					}
				}
			}
		}
		
		for(CompoundFile file : new ArrayList<>(fileMap.values())) {
			if(XattrFile.isValid(file.getFile())) {
				ExpressionHelper.executeIfNotNull(fileMap.get(XattrFile.getParentFile(file.getFile()).getName()), 
						c -> c.setXattrFile(file.getFile()),
						c -> orphanedFiles.add(file.getFile()));
				fileMap.remove(file.getFile().getName());
			}
			else {
				ExpressionHelper.addItem(groups, 
						getGroupName(file), 
						key -> new ArrayList<CompoundFile>()).add(file);
			}
		}
		
		_directories = Collections.unmodifiableList(dirs);
		_orphanedFiles = Collections.unmodifiableList(orphanedFiles);
		_files = Collections.unmodifiableList(new ArrayList<>(fileMap.values()));
		_groups = Collections.unmodifiableMap(groups);
		
	}
	
	protected String getGroupName(CompoundFile file) {
		return FileUtil.getNameWithoutExtension(file.getFile()).toLowerCase();
	}
	
	public List<CompoundFile> getGroup(CompoundFile file) {
		return ExpressionHelper.evaluateIfNotNull(_groups.get(getGroupName(file)), list -> Collections.unmodifiableList(list), null);
	}

}
