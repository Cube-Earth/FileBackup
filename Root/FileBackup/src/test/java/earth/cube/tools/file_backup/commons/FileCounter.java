package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import earth.cube.tools.file_backup.Globals;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class FileCounter {
	
	private File _rootDir;
	
	protected Map<String,Set<String>> _counters = new HashMap<>();
	
	@Getter
	protected Set<String> _deletedFiles = new HashSet<>();
	
	@Getter
	protected Set<String> _archivedFiles = new HashSet<>();

	@Getter
	protected Set<String> _housekeeperFiles = new HashSet<>();
	
	@Getter
	protected Set<String> _mainFiles = new HashSet<>();

	@Getter
	protected Set<String> _currentFiles = new HashSet<>();
	
	protected Stack<Context> _stack = new Stack<>();

	protected File _baseDir;
	
	@AllArgsConstructor
	protected static class Context {
		public File _baseDir;
		public Set<String> _files;
	}
	
	
	public FileCounter(File rootDir) {
		_rootDir = rootDir;
		_deletedFiles = prepare(Globals.HOUSEKEEPER_DIR_NAME + Globals.DELETED_DIRECTORY);
		_archivedFiles = prepare(Globals.HOUSEKEEPER_DIR_NAME + Globals.ARCHIVE_DIRECTORY);
		_housekeeperFiles = prepare(Globals.HOUSEKEEPER_DIR_NAME);
		_mainFiles = prepare("");
		countFiles(rootDir);
	}
	
	private void countFiles(File dir) {
		String sRelPath = FileUtil.getRelativePath(_rootDir, dir);
		Set<String> i = _counters.get(sRelPath);
		if(i != null) {
			if(_currentFiles != null)
				_stack.push(new Context(_baseDir, _currentFiles));
			_currentFiles = i;
			_baseDir = new File(_rootDir, sRelPath);
		}
		
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				countFiles(file);
			}
			else {
				if(!file.getName().equals(".DS_Store")) {
					String sRelFilePath = FileUtil.getRelativePath(_baseDir, file);
					_currentFiles.add(sRelFilePath);
				}
			}
		}
		
		if(i != null && sRelPath.length() > 0) {
			Context ctx = _stack.pop();
			_currentFiles = ctx._files;
			_baseDir = ctx._baseDir;
		}
	}

	protected Set<String> prepare(String sRelPath) {
		Set<String> files = new HashSet<>();
		_counters.put(sRelPath, files);
		return files;
	}

	public int getDeletedCount() {
		return _deletedFiles.size();
	}
	
	public int getArchivedCount() {
		return _archivedFiles.size();
	}

	public int getHousekeeperCount() {
		return _housekeeperFiles.size();
	}

	public int getMainCount() {
		return _mainFiles.size();
	}
	
	protected String getDump(String sLabel, Set<String> files) {
		StringBuilder sb = new StringBuilder(sLabel + ':');
		files.stream().forEachOrdered( s -> sb.append("  ").append(s));
		return sb.toString();
	}
	
	public String getDeletedDump() {
		return getDump(getClass().getName() + " - deleted files:", _deletedFiles);
	}
	
	public String getArchivedDump() {
		return getDump(getClass().getName() + " - archived files:", _archivedFiles);
	}

	public String getHousekeeperDump() {
		return getDump(getClass().getName() + " - housekeeper files:", _housekeeperFiles);
	}

	public String getMainDump() {
		return getDump(getClass().getName() + " - default files:", _mainFiles);
	}
}
