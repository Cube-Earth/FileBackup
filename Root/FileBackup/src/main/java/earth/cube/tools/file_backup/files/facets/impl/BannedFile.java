package earth.cube.tools.file_backup.files.facets.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import earth.cube.tools.file_backup.database.RecordBannedFile;
import earth.cube.tools.file_backup.files.AdvertisedAction;
import earth.cube.tools.file_backup.files.IAdvertisedActionFacet;
import earth.cube.tools.file_backup.files.IFacetedFile;
import earth.cube.tools.file_backup.files.IFilterFacet;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;

@FacetInterface(dependsOn = { ILinuxFile.class })
public class BannedFile implements IAdvertisedActionFacet {
	
	protected IFacetedFile _host;
	protected Connection _conn;
	
	public BannedFile(IFacetedFile file) throws SQLException, IOException {
		_host = file;
		_conn = file.getPath().getFileSystem().getDatabase().getConnection();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public AdvertisedAction getAdvertisedAction() throws IOException, SQLException {
		ILinuxFile file = _host.getFacet(ILinuxFile.class);
		return RecordBannedFile.isBanned(_conn, file) ? AdvertisedAction.BAN_FILE : AdvertisedAction.NONE;
	}

}
