package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import earth.cube.tools.file_backup.commons.TimeSpanDueException;
import earth.cube.tools.file_backup.model.RecordFile;

public class MoveFilesToNas extends AbstractFileTreeAction {

	protected PrintStream _out;

	@Override
	protected void process(File fileToMove) throws IOException, SQLException {
		RecordFile file = new RecordFile(_parameters.getSourceScope(), fileToMove);
		file.chain(_parameters.getDestinationScope()).moveTo(_parameters.getSourceDirectory(), _parameters.getDestinationDirectory(), _out);
	}
	
	@Override
	protected void startIterate(File dir) throws TimeSpanDueException, IOException, SQLException {
		try(PrintStream sha256Out = new PrintStream(new File(_parameters.getSourceDirectory(), "sha256sum.txt"))) {
			_out = sha256Out;
			super.startIterate(dir);
		}
	}

}
