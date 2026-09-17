package earth.cube.tools.file_backup.object_mapper;

import java.lang.reflect.Field;

public class BeanField implements IAttributeGetter, IAttributeSetter {
	
	private Field _field;
	
	private AttributeType _type;

	public BeanField(Field f) {
		_field = f;
		_type = AttributeType.from(_field.getType());
	}
	
	@Override
	public AttributeType getType() {
		return _type;
	}
	
	@Override
	public void set(Object instance, Object value) throws IllegalArgumentException, IllegalAccessException {
		_field.set(instance, value);
	}

	@Override
	public Object get(Object instance) throws IllegalArgumentException, IllegalAccessException {
		return _field.get(instance);
	}
}
