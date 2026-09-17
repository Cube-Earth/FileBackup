package earth.cube.tools.file_backup.model;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.files.common.IContentKey;
import earth.cube.tools.file_backup.values.snapshots.ObserveValue;
import earth.cube.tools.file_backup.values.snapshots.RefreshDate;
import earth.cube.tools.file_backup.values.snapshots.ValueSnapshot;

public class LinuxInode implements IContentKey {

	protected Logger _log = LogManager.getLogger(getClass());
	
	@ObserveValue()
	protected String _sSha256;
	
	protected DataSource _sha256Source;

	@ObserveValue()
	protected Date _sha256Updated;

	@ObserveValue(name = "ctime")
	protected Date _changedTime;

	@ObserveValue(name = "id")
	protected long _nId;

	@ObserveValue(categories= {"metrics"})
	protected long _nSize;

	@ObserveValue(categories= {"metrics"})
	protected String _sExtension;
	
	@RefreshDate()
	protected Date _dRefreshDate;

	protected long _nDeviceId;
	protected long _nRemoteDeviceId;
	
	protected ValueSnapshot _snapshot = new ValueSnapshot(this, LinuxInode.class);

	
	protected LinuxInode() {
	}
		
	
	public LinuxInode(LinuxFile file) throws IOException, SQLException {
		_dRefreshDate = new Date();
		init(file);
		_snapshot.record();
	}
	
	protected void init(LinuxFile file) throws IOException, SQLException {
		update(file);
	}
	
	public static long getId(LinuxFile file) {
		Map<String,Object> fileAttrs = file.getAttributes();
		return (Long) fileAttrs.get("ino");
	}
	
	
	public void updateByFile(LinuxFile file) throws IOException, SQLException {
		boolean bCalcSha256 = false;
		
		Map<String,Object> fileAttrs = file.getAttributes();
		if(_nId == 0) {
			_log.debug("updateByFile: new file");
			_nId = (Long) fileAttrs.get("ino");
			_nDeviceId = (Long) fileAttrs.get("dev");
			_nRemoteDeviceId = (Long) fileAttrs.get("rdev");
			bCalcSha256 = true;
		}
		else {
			if(((Long) fileAttrs.get("ino")) != _nId)
				throw new IllegalStateException(String.format("Updates only allowed on same Inode (inode: %s <-> %s)!", _nId, (Long) fileAttrs.get("ino")));
			if(_nDeviceId != 0 && ((Long) fileAttrs.get("dev")) != _nDeviceId)
				throw new IllegalStateException(String.format("Updates only allowed on same Inode (dev: %s <-> %s)!", _nDeviceId, (Long) fileAttrs.get("dev")));
			if(_nRemoteDeviceId != 0 && ((Long) fileAttrs.get("rdev")) != _nRemoteDeviceId)
				throw new IllegalStateException(String.format("Updates only allowed on same Inode (rdev: %s <-> %s)!", _nRemoteDeviceId, (Long) fileAttrs.get("rdev")));
		}

		_nSize = (Long) fileAttrs.get("size");
		_sExtension = file.getExtension();
		bCalcSha256 |= _snapshot.hasChanged("metrics");
		
		_changedTime = new Date(((FileTime) fileAttrs.get("ctime")).toMillis());
		if(_snapshot.hasChanged("ctime"))
			bCalcSha256 = true;
		
		if(bCalcSha256) {
			_log.debug("updateByFile: recalc sha256");
			_sSha256 = file.getSha256(_changedTime);
			_sha256Updated = _changedTime;
			_sha256Source = DataSource.CALCULATED;
			
			notifySha256Updated();
		}
	}
	
	
	protected void notifySha256Updated() throws IOException, SQLException {
	}


	public String getCalculatedSha256(LinuxFile file) throws IOException {
		if(file.getNodeId() != _nId)
			throw new IllegalArgumentException();
		if(_sha256Source != DataSource.CALCULATED) {
			_sSha256 = file.getSha256(_changedTime);
			_sha256Updated = _changedTime;
			_sha256Source = DataSource.CALCULATED;
		}
		return _sSha256;
	}
	
	public void update(LinuxFile file) throws IOException, SQLException {
		_dRefreshDate = new Date();
		updateByFile(file);
	}
	
	
	public boolean isDirty() {
		return _snapshot.hasChanged();
	}

	public long getId() {
		return _nId;
	}

	public long getSize() {
		return _nSize;
	}
	
	public Date getChangedTime() {
		return _changedTime;
	}

	public long getDeviceId() {
		return _nDeviceId;
	}

	public long getRemoteDeviceId() {
		return _nRemoteDeviceId;
	}
	
	public String getSha256() {
		return _sSha256;
	}
	
	public DataSource getSha256Source() {
		return _sha256Source;
	}
	
	public String getExtension() {
		return _sExtension;
	}
	
	public boolean hasChanged(LinuxInode inode) {
		return !inode.getChangedTime().equals(_changedTime);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof LinuxInode && _nId == ((LinuxInode) obj).getId();
	}
	
	@Override
	public int hashCode() {
		return ((Long)_nId).hashCode();
	}


	public String getKey() {
		return new InodeKey(_sSha256, _sExtension, _nSize).toString();
	}
	
	public static String getKey(String sSha256, String sExtension, long nSize) {
		return new InodeKey(sSha256, sExtension, nSize).toString();
	}
}
