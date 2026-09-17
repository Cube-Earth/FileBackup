package earth.cube.tools.file_backup.object_mapper.adapter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ResultSetAdapter implements IAdapter<ResultSet> {
	
	private ResultSet _rs;

	@Override
	public void setObject(ResultSet rs) {
		_rs = rs;
	}
	
	@Override
	public List<String> getDefaultAttributes() throws Exception {
		List<String> attrs = new ArrayList<>();
		ResultSetMetaData m = _rs.getMetaData();
		int n = m.getColumnCount();
		for(int i = 1; i <= n; i++) {
			attrs.add(m.getColumnLabel(i));
		}
		return Collections.unmodifiableList(attrs);
		
	}
	
	@Override
	public boolean getBoolean(String sName) throws Exception {
		return _rs.getBoolean(sName);
	}

	@Override
	public int getInt(String sName) throws Exception {
		return _rs.getInt(sName);
	}

	@Override
	public long getLong(String sName) throws Exception {
		return _rs.getLong(sName);
	}

	@Override
	public double getDouble(String sName) throws Exception {
		return _rs.getDouble(sName);
	}

	@Override
	public String getString(String sName) throws Exception {
		return _rs.getString(sName);
	}

	@Override
	public Date getDate(String sName) throws Exception {
		return Date.from(_rs.getTimestamp(sName).toInstant());
	}


}
