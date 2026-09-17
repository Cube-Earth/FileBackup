package earth.cube.tools.file_backup.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.Assertion;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Sha256Util;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractLinuxTestFile {
	
	@Getter
	protected File _file;
	
	@Getter @Setter
	private Date _date = new Date();
	
	private String _sExtension;
	
	@Getter
	protected byte[] _content;
	
	@Getter
	protected String _sSha256;

	protected String _sLogicalName;
	
	
	public AbstractLinuxTestFile(String sLogicalName, String sContent) {
		_sLogicalName = sLogicalName;
		_content = sContent.getBytes(StandardCharsets.UTF_8);
		try {
			_sSha256 = Sha256Util.getChecksum(_content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public AbstractLinuxTestFile(String sLogicalName, byte[] content) throws IOException {
		_sLogicalName = sLogicalName;
		_content = content;
		_sSha256 = _content == null ? null : Sha256Util.getChecksum(_content);
	}
	
	public AbstractLinuxTestFile(String sLogicalName, File contentFile) throws IOException {
		_sLogicalName = sLogicalName;
		_content = FileUtil.read(contentFile);
		_sSha256 = Sha256Util.getChecksum(_content);
	}
	
	public boolean isGenerated() {
		return false;
	}
	
	public void create(File file) throws IOException, SQLException {
		if(_file != null)
			throw new IllegalStateException("already used!");
		
		_file = file;
		_sExtension = FileUtil.getExtension(file);
		
		_file.getParentFile().mkdirs();
		FileUtil.write(file, getContent());
		_file.setLastModified(_date.getTime());
		verify();
	}
	
	public AbstractLinuxTestFile copy() {
		try {
			return getClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public AbstractLinuxTestFile copy(boolean bKeepDate) {
		AbstractLinuxTestFile file = copy();
		if(bKeepDate)
			file.setDate(getDate());
		return file;
	}

	public LinuxFile getLinuxFile() throws IOException, SQLException {
		return new LinuxFile(_file);
	}
	
	public void verify() throws IOException, SQLException {
		LinuxFile linuxFile = getLinuxFile();
		
		assertFalse(linuxFile._bDir);
		assertTrue(linuxFile._bFile);
		assertFalse(linuxFile._bSymLink);
		Assertion.create().expected(_date).actual(_date);
		Assertion.create().expected(_date).actual(linuxFile._dLastModified).truncateMillis().checkEquals();
		assertEquals(_file, linuxFile._file);
		assertNotNull(linuxFile._inode);
	
		assertNotNull(linuxFile._inode._changedTime);
// replaced by next line:		assertTrue(!_linuxFile._inode._changedTime.before(_date));
		Assertion.create().expected(_date).actual(linuxFile._inode._changedTime).truncateMillis().checkBeforeOrEquals();
		assertNotSame(0, linuxFile._inode._nDeviceId);
		assertNotSame(0, linuxFile._inode._nRemoteDeviceId);
		assertNotSame(0, linuxFile._inode._nId);
		assertEquals(_sExtension, linuxFile._inode._sExtension);
		assertEquals(getContent().length, linuxFile._inode._nSize);
		assertEquals(DataSource.CALCULATED, linuxFile._inode._sha256Source);
		assertNotNull(linuxFile._inode._sha256Updated);
// replaced by next line:		assertTrue(!_linuxFile._inode._sha256Updated.before(_date));
		Assertion.create().expected(_date).actual(linuxFile._inode._sha256Updated).truncateMillis().checkBeforeOrEquals();

		assertNotSame(0, linuxFile._nDeviceId);
		assertNotSame(0, linuxFile._nRemoteDeviceId);
		assertEquals(1, linuxFile._nNlink);

		assertEquals(getSha256(), linuxFile._sSha256);
		assertNotNull(linuxFile._sha256Updated);
// replaced by next line:		assertTrue(!_linuxFile._sha256Updated.before(_date));
		Assertion.create().expected(_date).actual(linuxFile._sha256Updated).truncateMillis().checkBeforeOrEquals();

		assertFalse(linuxFile._snapshot.hasChanged());
	}	
	
	public String getDump() {
		try {
			return String.format("%2s  %s", getLinuxFile().getLinkCount(), _sLogicalName);
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
