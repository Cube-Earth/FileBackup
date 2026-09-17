package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.model.RecordFile;

public class CopyFilesToNas extends AbstractFileTreeAction {
	
	@Override
	protected void process(File file) throws IOException, SQLException {
		RecordFile baseFile = new RecordFile(_parameters.getSourceScope(), file);
		baseFile.chain(_parameters.getDestinationScope()).copyTo(_parameters.getSourceDirectory(), _parameters.getDestinationDirectory());
	}

}
