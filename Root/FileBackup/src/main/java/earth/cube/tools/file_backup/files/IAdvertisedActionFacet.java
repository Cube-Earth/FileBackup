package earth.cube.tools.file_backup.files;

import java.io.IOException;
import java.sql.SQLException;

public interface IAdvertisedActionFacet extends IFacet {
	
	AdvertisedAction getAdvertisedAction() throws IOException, SQLException;

}
