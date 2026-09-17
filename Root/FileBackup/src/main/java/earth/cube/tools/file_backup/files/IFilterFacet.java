package earth.cube.tools.file_backup.files;

import java.io.IOException;
import java.sql.SQLException;

public interface IFilterFacet extends IFacet {
	
	boolean isFilteredOut() throws IOException, SQLException;

}
