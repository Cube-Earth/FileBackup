package earth.cube.tools.file_backup.object_mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanSetter implements IAttributeSetter {
	
	private Method _method;
	private AttributeType _type;
	
	public BeanSetter(Method m) {
		_method = m;
		if(m.getParameterCount() != 1)
			throw new IllegalStateException();
		_type = AttributeType.from(m.getParameterTypes()[0]);
	}

	@Override
	public AttributeType getType() {
		return _type;
	}

	@Override
	public void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		_method.invoke(instance, value);
	}

}
