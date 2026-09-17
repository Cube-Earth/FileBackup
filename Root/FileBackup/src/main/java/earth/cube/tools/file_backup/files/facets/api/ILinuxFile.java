package earth.cube.tools.file_backup.files.facets.api;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.IValidator;
import earth.cube.tools.file_backup.files.IFacet;
import earth.cube.tools.file_backup.files.common.IInodeContent;
import earth.cube.tools.file_backup.filesystem.features.IInodeContentInformation;

public interface ILinuxFile extends IValidator, IInodeContentInformation, IFacet  {

	File getFile();

	long getInodeId();

	Date getLastModified();

	Date getRefreshDate();

	boolean isSymLink();

	boolean isDir();

	int getNlink();

	long getDeviceId();

	long getRemoteDeviceId();

	long getSize();

	Date getInodeChangedTime();
	
	void calculateSha256() throws IOException;

	void setSha256(IInodeContent cnt);
}