package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.TimeSpanDueException;
import earth.cube.tools.file_backup.model.RecordFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FindClonedDirectoriesAction extends AbstractFileTreeAction {
	
	protected Map<String,Directory> _dirs = new HashMap<>();
	protected DirectoryFactory _factory = new DirectoryFactory();
	protected Set<String> _ownDirs = new HashSet<>();
	protected Set<String> _ownFiles = new HashSet<>();
	protected int _nTotal;
	
	
	protected static class Directory {
		
		@Getter
		private String _sPath;
		
		protected Set<Directory> _folders = new HashSet<>();
		
		protected Set<String> _files = new HashSet<>();

		public Directory(String sPath) {
			_sPath = sPath;
		}
		
		public void addFolder(Directory folder) {
			_folders.add(folder);
		}
		
		public void addFile(String sNodeKey) {
			_files.add(sNodeKey);
		}
		
		public Set<String> getMatchedDescendants(Set<String> files) {
			files.addAll(_files);
			for(Directory folder : _folders) {
				folder.getMatchedDescendants(files);
			}
			return files;
		}
		
		public int getMatchedDescendantsCount() {
			return getMatchedDescendants(new HashSet<>()).size();
		}
		
		@Override
		public int hashCode() {
			return _sPath.hashCode();
		}
	}
	
	
	protected static class MatchedDirectory {
		
		@Getter @Setter
		protected int _nMatched;
		
		@Getter @Setter
		protected int _nTotal;
		
		@Getter @Setter
		protected int _nUniqueTotal;

		@Getter @Setter
		protected float _fRatio;
		
		@Getter @Setter
		protected String _sPath;
		
		@Getter @Setter
		protected Directory _directory;
		

		public MatchedDirectory(int nTotal, int nUniqueTotal, Directory dir) {
			_nTotal = nTotal;
			_nUniqueTotal = nUniqueTotal;
			_directory = dir;
			_nMatched = dir.getMatchedDescendantsCount();
			_fRatio = (float) _nMatched / _nUniqueTotal * 100;
			_sPath = dir.getPath();
		}
		
		@Override
		public String toString() {
			String sTotal = Integer.toString(_nTotal);
			
			return String.format("%5.2f    %" + sTotal.length() + "s/%s (%s)   %s", _fRatio, _nMatched, _nUniqueTotal, _nTotal, _sPath);
		}
		
	}

	
	protected class DirectoryFactory {
		
		protected Directory _noParent = new Directory(null);

		
		public Directory getByFolderPath(String sPath) {
			if(sPath == null)
				return _noParent; // for easier handling
			
			Directory dir = _dirs.get(sPath);
			if(dir == null) {
				dir = new Directory(sPath);
				_dirs.put(sPath, dir);
				getByFolderPath(FileUtil.getParentDirectory(sPath)).addFolder(dir);
			}
			return dir;
		}

		public Directory getByFile(String sFilePath) {
			return getByFolderPath(FileUtil.getParentDirectory(sFilePath));
		}
		
		public void addFile(RecordFile file) {
			getByFile(file.getNeutralPath()).addFile(file.getInode().getKey());
		}
		
		public List<MatchedDirectory> getMatchedDirectories() {
			List<MatchedDirectory> matched = new ArrayList<>();
			
			_dirs.keySet().removeAll(FileUtil.getAllParentDirectories(_parameters.getSourceDirectory().getAbsolutePath(), true));
			_dirs.keySet().removeAll(_ownDirs);
			
			for(Directory dir : _dirs.values()) {
				matched.add(new MatchedDirectory(_nTotal, _ownFiles.size(), dir));
			}
			
			Collections.sort(matched, new Comparator<MatchedDirectory>() {

				@Override
				public int compare(MatchedDirectory d1, MatchedDirectory d2) {
					int n = -Integer.compare(d1.getMatched(), d2.getMatched());
					if(n == 0)
						n = d1.getPath().compareTo(d2.getPath());
					return n;
				}
				
			});
			
			return matched;
		}
		
		public void printMatchedDirectories() {
			for(MatchedDirectory dir : getMatchedDirectories()) {
				_parameters.getOut().println(dir.toString());
			}
		}
		
	}
	
	
	
	@Override
	protected void process(File fileToCheck) throws IOException, SQLException {
		log.debug("process: file = " + fileToCheck.getAbsolutePath());
		RecordFile file = new RecordFile(_parameters.getSourceScope(), fileToCheck);
		if(!file.isValid() || FileCollection.isXattrSidecar(fileToCheck))  // is anyway hidden to the user
			return;
		
		_ownDirs.add(FileUtil.getParentDirectory(file.getNeutralPath()));
		_ownFiles.add(file.getInode().getKey());
		_nTotal++;
		
		FileCollection.create().file(file).addFilesWithSameSha256().removeFile().execute( f -> {
			_factory.addFile(f);
		});		
	}



	@Override
	protected void startIterate(File dir) throws TimeSpanDueException, IOException, SQLException {
		super.startIterate(dir);
		
		_factory.printMatchedDirectories();
	}


}
