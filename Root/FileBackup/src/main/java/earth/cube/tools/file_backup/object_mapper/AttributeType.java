package earth.cube.tools.file_backup.object_mapper;

import java.util.Date;

public enum AttributeType {
	
	BOOLEAN,
	INTEGER,
	LONG,
	DOUBLE,
	STRING,
	DATE,
	UNKNOWN;
	
	
	public static AttributeType from(Class<?> clazz) {
		if(clazz.isArray())
			return UNKNOWN;
		if(clazz.equals(Boolean.class) || clazz.equals(boolean.class))
			return BOOLEAN;
		if(clazz.equals(Integer.class) || clazz.equals(int.class))
			return INTEGER;
		if(clazz.equals(Long.class) || clazz.equals(long.class))
			return LONG;
		if(clazz.equals(Float.class) || clazz.equals(Double.class) || clazz.equals(float.class) || clazz.equals(double.class))
			return DOUBLE;
		if(clazz.equals(String.class))
			return STRING;
		if(Date.class.isAssignableFrom(clazz))
			return DATE;
		return UNKNOWN;
	}
	
	
}
