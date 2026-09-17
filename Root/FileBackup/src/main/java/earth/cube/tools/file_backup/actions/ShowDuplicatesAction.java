package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.model.RecordFile;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ShowDuplicatesAction extends AbstractFileTreeAction {
	
	@Override
	protected void process(File fileToCheck) throws IOException, SQLException {
		log.debug("process: file = " + fileToCheck.getAbsolutePath());
		RecordFile file = new RecordFile(_parameters.getSourceScope(), fileToCheck);
		_parameters.getOut().println(file.getNeutralPath());
		
		FileCollection.create().file(file).addFilesWithSameSha256().removeFile().sort().execute(f ->  {
			_parameters.getOut().println("    " + f.getNeutralPath());
		});
	}

}