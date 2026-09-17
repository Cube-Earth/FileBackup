package earth.cube.tools.file_backup.files.facets.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Sha256Util;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.facets.api.IGenericFile;
import lombok.Getter;

@FacetInterface(provides=IGenericFile.class)
public class GenericFile implements IGenericFile {
	
	@Getter
	protected File _file; 

	@Getter
	protected String _sName;
	
	@Getter
	protected String _sExtension;

	@Getter
	private boolean _bValid;

	private String _sSha256;

	private Date _sha256Updated;
	
	
	public GenericFile(File file) {
		_file = file;
		_bValid = file != null && file.exists() && file.isFile();
		if(_bValid) {
			_sName = FileUtil.getNameWithoutExtension(_file);
			_sExtension = FileUtil.getExtension(_file);
		}
	}

	@Override
	public String getSha256(Date treshold) throws IOException {
		if(_sSha256 == null || treshold == null || treshold.after(_sha256Updated)) {
			_sha256Updated = new Date();
			_sSha256 = Sha256Util.getChecksum(_file);
		}
		return _sSha256;
	}
	
	

}
