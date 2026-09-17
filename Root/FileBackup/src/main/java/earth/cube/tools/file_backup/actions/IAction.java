package earth.cube.tools.file_backup.actions;

import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.commons.TimeSpanDueException;

public interface IAction {

	void setParameters(ActionParameters params);
	
	ActionParameters getParameters();

	void execute() throws IOException, SQLException, TimeSpanDueException;
	
	
}
