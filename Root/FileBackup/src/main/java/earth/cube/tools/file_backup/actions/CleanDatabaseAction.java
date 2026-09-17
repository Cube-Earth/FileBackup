package earth.cube.tools.file_backup.actions;

import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.Query;

public class CleanDatabaseAction extends AbstractAction {

	@Override
	public void execute() throws IOException, SQLException {
		Scope scope = _parameters.getSourceScope();
		
	    Query.perform(scope.getConnection(), q -> q.query(
	    	"delete from file where valid = 0"
	    ).execute());

	    Query.perform(scope.getConnection(), q -> q.query(
	    	"delete from inode where id in (select id from inode i where not exists(select * from file where inode=id))"
	    ).execute());
	}

}
