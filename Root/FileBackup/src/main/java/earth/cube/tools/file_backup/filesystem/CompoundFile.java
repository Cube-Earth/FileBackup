package earth.cube.tools.file_backup.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.IValidator;
import earth.cube.tools.file_backup.files.FileSystem;
import earth.cube.tools.file_backup.files.facets.api.IGenericFile;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;
import earth.cube.tools.file_backup.files.facets.impl.GenericFile;
import earth.cube.tools.file_backup.files.facets.impl.LinuxFile;
import lombok.Getter;
import lombok.Setter;

public class CompoundFile {
	
	@Getter
	protected File _file;
	
	@Getter
	protected boolean _bValid;
	
	@Getter @Setter
	protected File _xattrFile;
	
	@Getter
	protected IGenericFile _genericFile;
		
	protected FileSystem _fs;

	@Getter
	private String _sNeutralPath;

	private Map<Class<?>,Object> _features = new HashMap<>();

	
	public CompoundFile(FileSystem fs, String sNeutralPath, File file) throws IOException {
		_fs = fs;
		_file = file;
		_sNeutralPath = FileUtil.getRelativePath(_fs.getBaseDirectory(), file);
		if(_sNeutralPath == null)
			throw new IllegalStateException(String.format("'%s' is not underneath '%s'!", file.getAbsolutePath(), _fs.getBaseDirectory().getAbsolutePath()));
		
		checkValid();
		addFeature(IGenericFile.class, new GenericFile(file));		
		addFeature(ILinuxFile.class, new LinuxFile(file));

	}
	
	protected <T extends IValidator> void addFeature(Class<?> key, T feature) {
		if(_bValid && feature.isValid()) {
			_features.put(key, feature);
		}
		else {
			_bValid = false;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getFeature(T key) {
		return (T) _features.get(key);
	}

	
	protected void checkValid() throws IOException {
		 _bValid &= !Globals.isProtected(_file);
		 _bValid &= !XattrFile.isValid(_file);
		 _bValid &= !_file.getName().equals(".DS_Store");
	}
	
	public void delete() {
		if(_genericFile.isValid()) {
			if(Globals.isProtected(_file)) {
				throw new IllegalStateException("Don't act on protected files: " + _file.getAbsolutePath());
			}
			else {
				if(!_file.delete()) {
					throw new IllegalStateException("Could not delete file: " + _file.getAbsolutePath());
				}
			}
		}
	}

	public String getSha256() throws IOException {
		return _genericFile.getSha256(_linuxFile.getChangedTime()); // inode is irrelevant. If inode changes, changed time will change as well.
	}


}
