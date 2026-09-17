package earth.cube.tools.file_backup.commons;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

public class FileName {
	
	@Getter @Setter
	protected File _parentDirectory;
	
	@Getter @Setter
	protected String _sPrefix;

	@Getter @Setter
	protected String _sName;
	
	@Getter @Setter
	protected String _sSuffix;

	@Getter @Setter
	protected int _nUnifier;

	@Getter @Setter
	protected String _sExtension;

	
	public FileName(File file) {
		this(file.getAbsolutePath().replace('\\', '/'));
	}

	public FileName(String sPath) {
		int i = sPath.lastIndexOf('/');
		int j = sPath.lastIndexOf('.');
		
		_parentDirectory = i == -1 ? null : new File(sPath.substring(0, i));
		_sName = sPath.substring(i == -1 ? 0 : i + 1, j == -1 || (i != -1 && j < i) ? sPath.length() : j);
		_sExtension = j > i ? sPath.substring(j) : "";
	}
	
	public String getLoweredExtensions() {
		return _sExtension.toLowerCase();
	}
	
	public File getFile() {
		return new File(_parentDirectory,
				(_sPrefix != null ? _sPrefix : "") +
				_sName + 
				(_sSuffix != null ? _sSuffix : "") +
				(_nUnifier == 0 ? "" : " (" + _nUnifier + ')') +
				_sExtension);
	}
	
	public File findUniqueFileName() {
		while(getFile().exists()) {
			_nUnifier++;
		}
		return getFile();
	}

}
