package earth.cube.tools.file_backup.commons;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class TestResultSet {
	
	public static String getRow(ResultSet rs) throws SQLException {
		StringBuilder sb = new StringBuilder();
		ResultSetMetaData m = rs.getMetaData();
		int n = m.getColumnCount();

		for (int i = 1; i <= n; i++ ) {
			if(i > 1)
				sb.append("|");
			sb.append(rs.getString(i));
		}
		return sb.toString();
	}

}
