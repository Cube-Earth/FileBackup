package earth.cube.tools.file_backup.object_mapper.adapter;

import java.util.Date;
import java.util.List;

public interface IAdapter<T> {

	void setObject(T obj);

	List<String> getDefaultAttributes() throws Exception;

	boolean getBoolean(String sName) throws Exception;

	int getInt(String sName) throws Exception;

	long getLong(String sName) throws Exception;

	double getDouble(String sName) throws Exception;

	String getString(String sName) throws Exception;

	Date getDate(String sName) throws Exception;


}