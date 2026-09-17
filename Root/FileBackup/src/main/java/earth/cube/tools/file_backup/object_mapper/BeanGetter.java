package earth.cube.tools.file_backup.object_mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanGetter implements IAttributeGetter {
	
	private Method _method;
	private AttributeType _type;
	
	public BeanGetter(Method m) {
		_method = m;
		if(m.getParameterCount() != 0)
			throw new IllegalStateException();
		_type = AttributeType.from(m.getReturnType());
	}

	@Override
	public AttributeType getType() {
		return _type;
	}

	@Override
	public Object get(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return _method.invoke(instance);
	}

}
