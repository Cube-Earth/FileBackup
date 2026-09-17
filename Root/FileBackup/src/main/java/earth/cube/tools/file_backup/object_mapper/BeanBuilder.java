package earth.cube.tools.file_backup.object_mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import earth.cube.tools.file_backup.object_mapper.adapter.IAdapter;

public class BeanBuilder<T, U> {
	
	private static Map<String,BeanClass<?>> _clazzes = new HashMap<>();

	public static <T, U> BeanBuilder2<T, U> create(Class<? extends IAdapter<T>> adapterClass, Class<U> objectClass) {
		return new BeanBuilder2<T, U>().adapter(adapterClass).clazz(objectClass);
	}
	
	private Class<U> _clazz;
	
	private String _sScope = "";
	
	private boolean _bRaiseExceptions;

	private Class<? extends IAdapter<T>> _adapterClass;

	private List<String> _attributes;
	
	
	public BeanBuilder<T, U> clazz(Class<U> clazz) {
		_clazz = clazz;
		return this;
	}

	public BeanBuilder<T, U> scope(String sScope) {
		_sScope = sScope;
		return this;
	}

	public BeanBuilder<T, U> raiseExceptions(boolean bRaiseExceptions) {
		_bRaiseExceptions = bRaiseExceptions;
		return this;
	}
	
	public BeanBuilder<T, U> adapter(Class<? extends IAdapter<T>> clazz) {
		_adapterClass = clazz;
		return this;
	}
	
	public BeanBuilder<T, U> attributes(List<String> attributes) {
		_attributes = new ArrayList<>(attributes);
		return this;
	}

	public BeanBuilder<T, U> attributes(String... saAttributes) {
		_attributes = Arrays.asList(saAttributes);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Bean<T, U> build() throws Exception {
		String sKey = _clazz.getCanonicalName() + "#" + _sScope + "#" + _bRaiseExceptions;
		BeanClass<U> clazz;
		synchronized(_clazzes) {
			clazz = (BeanClass<U>) _clazzes.get(sKey);
			if(clazz == null) {
				clazz = new BeanClass<>(_clazz, _sScope, _bRaiseExceptions);
				_clazzes.put(sKey, clazz);
			}
		}
		Bean<T,U> bean = new Bean<>();
		bean.setClass(clazz);
		bean.setAdapter(_adapterClass);
		bean.setAttributes(_attributes);
		return bean;
	}

}
