package earth.cube.tools.file_backup.object_mapper;

import java.util.List;

import earth.cube.tools.file_backup.object_mapper.adapter.IAdapter;

public class Bean<T, U> {
	
	private BeanClass<U> _clazz;
	private Class<? extends IAdapter<T>> _adapterClass;
	private List<String> _attributes;

	public void setClass(BeanClass<U> clazz) {
		_clazz = clazz;
	}

	public void setAdapter(Class<? extends IAdapter<T>> adapterClass) {
		_adapterClass = adapterClass;
	}
	
	public void setAttributes(List<String> attributes) {
		_attributes = attributes;
	}
	
	public U adopt(T source) throws Exception {
		IAdapter<T> adapter = _adapterClass.getDeclaredConstructor().newInstance();
		adapter.setObject(source);
		
		if(_attributes == null)
			_attributes = adapter.getDefaultAttributes();
		
		U instance = _clazz.newInstance();
		for(String sName : _attributes) {
			_clazz.setValue(instance, sName, adapter);
		}
		
		_clazz.executeAfterLoading(instance);
		
		return instance;
	}

}
