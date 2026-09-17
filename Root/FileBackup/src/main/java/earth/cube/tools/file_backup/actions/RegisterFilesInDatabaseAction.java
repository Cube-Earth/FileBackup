package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.Counter;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.TimeSpanDueException;
import earth.cube.tools.file_backup.model.RecordFile;

public class RegisterFilesInDatabaseAction extends AbstractFileTreeAction {
	
	@Override
	protected void process(File file) throws IOException, SQLException {
		new RecordFile(_parameters.getSourceScope(), file);
	}
	
	
	@Override
	protected void startIterate(File dir) throws TimeSpanDueException, IOException, SQLException {
		Date date = new Date();
		
		// check existent files
		super.iterate(dir);

		// check non-existent files (haven't matched before)
		String sSql = "select * from file where updated < " + Query.getDateClause(date);
		

		Counter n = new Counter();
		
		Query.perform(_parameters.getSourceScope().getConnection(), q -> q.query(sSql).execute().process( rs -> {
			new RecordFile(_parameters.getSourceScope(), rs);
			if((n.increment() % 1000) == 0)
				try {
					_parameters.getTimeSpan().checkIfDue();
				} catch (TimeSpanDueException e) {
					throw new IllegalStateException(e);
				}
		}));		
	}

	
	
}
