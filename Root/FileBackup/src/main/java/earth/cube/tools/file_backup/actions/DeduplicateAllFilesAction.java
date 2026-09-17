package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.model.RecordFile;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeduplicateAllFilesAction extends AbstractFileTreeAction {
	
	@Override
	protected void process(File fileToCheck) throws IOException, SQLException {
		log.debug("process: file = " + fileToCheck.getAbsolutePath());
		RecordFile file = new RecordFile(_parameters.getSourceScope(), fileToCheck);
		file.chain().deduplicate(0);
	}

}