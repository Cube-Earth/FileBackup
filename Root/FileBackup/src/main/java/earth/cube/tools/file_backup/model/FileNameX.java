package earth.cube.tools.file_backup.model;

import java.io.File;

import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileUtil;
import lombok.Getter;
import lombok.Setter;

public class FileNameX {

	@Getter @Setter
	protected File _directory;
	
	@Getter @Setter
	protected String _sPrefix;
	
	@Getter @Setter
	protected String _sName;
	
	@Getter @Setter
	protected String _sExtension;
	
	@Getter @Setter
	protected int _nSuffix = -1;

	@Getter @Setter
	private RecordFile _recordFile;

	
	public FileNameX(RecordFile rfile) {
		_recordFile = rfile;
		File file = rfile.getFile();
		_directory = file.getParentFile();
		_sExtension = FileUtil.getRawExtension(file);
		String sName = FileUtil.getNameWithoutExtension(file);
		if(sName.startsWith(FileCollection.XATTR_PREFIX)) {
			_sPrefix = FileCollection.XATTR_PREFIX;
			_sName = sName.substring(0, FileCollection.XATTR_PREFIX.length());
		}
		else {
			_sName = sName;
		}
	}
	
	public File getFile() {
		return new File(_directory, (_sPrefix == null ? "" : _sPrefix) + _sName + (_nSuffix == -1 ? "" : " (" + _nSuffix + ")") + (_sExtension == null ? "" : _sExtension));
	}
	
	public boolean exists() {
		return getFile().exists();
	}
	
}
