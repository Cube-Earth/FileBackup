package earth.cube.tools.file_backup.files.facets.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Sha256Util;
import earth.cube.tools.file_backup.commons.ValidationException;
import earth.cube.tools.file_backup.files.IFacetedFile;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.common.IContentKey;
import earth.cube.tools.file_backup.files.common.IInodeContent;
import earth.cube.tools.file_backup.files.common.IInodeKey;
import earth.cube.tools.file_backup.files.common.InodeKey;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;
import lombok.Getter;

@FacetInterface(provides=ILinuxFile.class)
public class LinuxFile implements ILinuxFile, IInodeContent {

	protected Logger _log = LogManager.getLogger(getClass());

	@Getter
	protected File _file;

	@Getter
	protected long _nInodeId;
	
	@Getter
	protected Date _dLastModified;
	
	@Getter
	protected Date _dRefreshDate;

	protected Map<String, Object> _attrs;

	@Getter
	protected boolean _bFile = false;

	@Getter
	protected boolean _bSymLink = false;

	@Getter
	protected boolean _bDir = false;

	@Getter
	protected int _nNlink;

	@Getter
	protected long _nDeviceId;
	
	@Getter
	protected long _nRemoteDeviceId;

	@Getter
	protected long _nSize;

	@Getter
	protected Date _dInodeChangedTime;
	
	@Getter
	protected boolean _bValid;
	
	@Getter
	protected String _sSha256;

	@Getter
	protected boolean _bChecksumVerified;
	
	@Getter
	protected String _sExtension;

	@Getter
	private IInodeKey _inodeKey;
	
	@Getter
	private IContentKey _contentKey;

	@Getter
	private boolean _bChanged;

	@Getter
	private IFacetedFile _host;
	
	


	public LinuxFile(IFacetedFile file) throws IOException, SQLException {
		_host = file;
		_file = file.getPath().getFile();
		refresh();
		_bChanged = false;
	}
	
	public boolean refresh() throws IOException, SQLException {
		_dRefreshDate = new Date();
		if(!_file.exists()) {
			_bValid = false;
			return false;
		}

		IInodeKey inode = new InodeKey(this);
		
		_attrs = Files.readAttributes(_file.toPath(), "unix:lastModifiedTime,isRegularFile,nlink,ino,size,dev,isSymbolicLink,rdev,isDirectory,ctime");
		_nInodeId = (Long) _attrs.get("ino");
		_dLastModified = Date.from(((FileTime) _attrs.get("lastModifiedTime")).toInstant());
		_bFile = (boolean) _attrs.get("isRegularFile");
		_bSymLink = (boolean) _attrs.get("isSymbolicLink");
		_bDir = (boolean) _attrs.get("isDirectory");
		_nNlink = (int) _attrs.get("nlink");
		_nDeviceId = (long) _attrs.get("dev");
		_nRemoteDeviceId = (long) _attrs.get("rdev");
		_nSize = (Long) _attrs.get("size");
		_sExtension = FileUtil.getExtension(_file);
		_dInodeChangedTime = new Date(((FileTime) _attrs.get("ctime")).toMillis());
		
		_bChanged = !inode.equals(this);   // changed or first-time init
		if(_bChanged) {
			_sSha256 = _host.getPath().getFileSystem().getCachedSha256(_host);
		}

		_bValid = true;
		return _bChanged;
	}
	
	public void calculateSha256() throws IOException {
		if(_sSha256 == null || _sSha256.length() == 0)
			_sSha256 = Sha256Util.getChecksum(_file);
	}
	
	public void setSha256(IInodeContent cnt) {
		_log.debug("setSha256: file = " + _file.getAbsolutePath());
		if(_sSha256 == null || _sSha256.length() == 0) {
			if(_nInodeId != cnt.getInodeId() || !_dInodeChangedTime.equals(cnt.getInodeChangedTime()))
				throw new ValidationException("Trying to set SHA256 for " + InodeKey.toString(this) + " from " + InodeKey.toString(cnt) + "!");		
			if(_nSize != cnt.getSize())
				throw new ValidationException("Sizes do not match: " + _nSize + " <> " + cnt.getSize());		
			_sSha256 = cnt.getSha256();
		}
		else
			throw new ValidationException("SHA256 is already set!");		
	}

}
