package earth.cube.tools.file_backup.actions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.model.RecordFile;

public class VerifyRegisteredFilesAction extends AbstractAction {

	private Date _dBefore;
	
	/**
	 * Idea to set the 'before' date in case the Register Files action just run
	 * before this action and so don't need to check the files touched by the
	 * Register Files action once again.
	 */
	public void setBeforeDate(Date dBefore) {
		_dBefore = dBefore;
	}

	
	@Override
	public void execute() throws IOException, SQLException {
		_parameters.getSourceScope().resetCache();
		
		String sSql;
		if(_dBefore == null)
			sSql = "select * from file"; // simple as that
		else
			sSql = "select * from file where updated < " + Query.getDateClause(_dBefore);
		
		Query.perform(_parameters.getSourceScope().getConnection(), q -> q.query(sSql).execute().process( rs -> {
			new RecordFile(_parameters.getSourceScope(), rs);
		}));		
	}

}
