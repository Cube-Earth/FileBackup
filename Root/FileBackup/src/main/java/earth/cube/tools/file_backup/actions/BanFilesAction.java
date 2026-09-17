package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.model.RecordFile;

public class BanFilesAction extends AbstractFileTreeAction {
	
	@Override
	protected void process(File file) throws IOException, SQLException {
		RecordFile baseFile = new RecordFile(_parameters.getSourceScope(), file);
		baseFile.chain().ban(_parameters.getRemark());
	}

}
