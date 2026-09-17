package earth.cube.tools.file_backup.object_mapper.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MapAdapter implements IAdapter<Map<String, Object>> {

	private Map<String, Object> _map;

	@Override
	public void setObject(Map<String, Object> map) {
		_map = map;
	}

	@Override
	public List<String> getDefaultAttributes() throws Exception {
		return new ArrayList<>(_map.keySet());
	}

	@Override
	public boolean getBoolean(String sName) throws Exception {
		return (boolean) _map.get(sName);
	}

	@Override
	public int getInt(String sName) throws Exception {
		return (int) _map.get(sName);
	}

	@Override
	public long getLong(String sName) throws Exception {
		return (long) _map.get(sName);
	}

	@Override
	public double getDouble(String sName) throws Exception {
		return (double) _map.get(sName);
	}

	@Override
	public String getString(String sName) throws Exception {
		return (String) _map.get(sName);
	}

	@Override
	public Date getDate(String sName) throws Exception {
		return (Date) _map.get(sName);
	}

}
