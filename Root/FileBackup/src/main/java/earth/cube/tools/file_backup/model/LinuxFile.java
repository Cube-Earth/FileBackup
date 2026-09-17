package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.commons.DebugScope;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Sha256Util;
import earth.cube.tools.file_backup.values.snapshots.ObserveValue;
import earth.cube.tools.file_backup.values.snapshots.RefreshDate;
import earth.cube.tools.file_backup.values.snapshots.ValueSnapshot;

public class LinuxFile {

	protected Logger _log = LogManager.getLogger(getClass());

	@ObserveValue()
	protected long _nInodeId;
	
	@ObserveValue()
	protected boolean _bValid;
	
	@ObserveValue()
	protected Date _dLastModified;
	
	@RefreshDate()
	protected Date _dRefreshDate;

	protected LinuxInode _inode;
	protected File _file;
	protected Map<String, Object> _attrs;
	protected String _sSha256;
	protected Date _sha256Updated;
	protected boolean _bFile = false;
	protected boolean _bSymLink = false;
	protected boolean _bDir = false;
	protected int _nNlink;
	protected long _nDeviceId;
	protected long _nRemoteDeviceId;
	protected ValueSnapshot _snapshot = new ValueSnapshot(this, LinuxFile.class);

	
	protected LinuxFile() {
	}

	public LinuxFile(File file) throws IOException, SQLException {
		_dRefreshDate = new Date();
		init(file);
		_snapshot.record();
	}
	
	protected void init(File file) throws IOException, SQLException {
		_file = file;
		update();
	}
	
	
	protected void updateByFile() throws IOException {
		if(Globals.isProtected(_file)) {
			throw new IllegalStateException("Don't act on protected files: " + _file.getAbsolutePath());
		}
		
		if(!_file.exists()) {
			_log.debug("updateByFile: file exists = false");
			_bValid = false;
			return;
		}

		if(_file.getName().equals(".DS_Store")) {
			_log.debug("updateByFile: .DS_Store file found");
			_bValid = false;
			return;
		}
		
		_attrs = Files.readAttributes(_file.toPath(), "unix:lastModifiedTime,isRegularFile,nlink,ino,size,dev,isSymbolicLink,rdev,isDirectory,ctime");
		_dLastModified = Date.from(((FileTime) _attrs.get("lastModifiedTime")).toInstant());
		_bFile = (boolean) _attrs.get("isRegularFile");
		_bSymLink = (boolean) _attrs.get("isSymbolicLink");
		_bDir = (boolean) _attrs.get("isDirectory");
		_nNlink = (int) _attrs.get("nlink");
		_nDeviceId = (long) _attrs.get("dev");
		_nRemoteDeviceId = (long) _attrs.get("rdev");
		
		if(DebugScope.has("debug") && _nInodeId != 0 && _nInodeId != (Long) _attrs.get("ino"))
			System.out.println("###");
		
		_nInodeId = (Long) _attrs.get("ino");
		
		_bValid = _bFile && !_bSymLink;
		_log.debug("updateByFile: file valid = " + _bValid);
	}
	
	protected void update() throws IOException, SQLException {
		_dRefreshDate = new Date();
		updateByFile();		
		if(_bValid)
			_inode = new LinuxInode(this);
		else {
			_nInodeId = 0;
			_inode = null;
		}
	}

	public boolean isDirty() {
		return _snapshot.hasChanged();
	}

	public File getFile() {
		return _file;
	}
	
	public LinuxInode getInode() {
		return _inode;
	}
	
	public Map<String, Object> getAttributes() {
		return _attrs;
	}

	public boolean isValid() {
		return _bValid;
	}
	
	public Date getLastModificationDate() {
		return _dLastModified;
	}
	
	public boolean isFile() {
		return _bFile;
	}

	public boolean isSymbolicLink() {
		return _bSymLink;
	}

	public boolean isDirectory() {
		return _bDir;
	}
	
	public int getLinkCount() {
		return _nNlink;
	}
	

	
	public String getSha256(Date treshold) throws IOException {
		if(_sSha256 == null || treshold == null || treshold.after(_sha256Updated)) {
			_sha256Updated = new Date();
			if(!_bFile || _bSymLink) {
				throw new IllegalStateException("Not a file: " + _file.getAbsolutePath());
			}
			_sSha256 = Sha256Util.getChecksum(_file);
		}
		return _sSha256;
	}
	
	
	public String getCalculatedSha256FromInode() throws IOException {
		return _inode.getCalculatedSha256(this);
	}
	
	
	public String getExtension() {
		return FileUtil.getExtension(_file);
	}

	public long getDeviceId() {
		return _nDeviceId;
	}

	public long getNodeId() {
		return _nInodeId;
	}

}
