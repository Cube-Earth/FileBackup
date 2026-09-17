package earth.cube.tools.file_backup.values.snapshots;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValueSnapshot {
	
	protected Map<String,Object> _values = new HashMap<>();
	protected Map<String,Set<String>> _categories = new HashMap<>();
	protected Object _obj;
	private Class<?> _class;
	protected Date _dRefreshDate = new Date();
	
	public ValueSnapshot(Object o, Class<?> clazz) {
		_obj = o;
		_class = clazz;
		record();
	}
	
	public void record() {
		_values.clear();
		_categories.clear();
		
		for(Field f : _class.getDeclaredFields()) {
			ObserveValue a = f.getDeclaredAnnotation(ObserveValue.class);
			if(a != null) {
				try {
					f.setAccessible(true);
					String sName = a.name().length() == 0 ? f.getName() : a.name();
					_values.put(sName, f.get(_obj));
					addCategories(sName, a.categories());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			RefreshDate d = f.getDeclaredAnnotation(RefreshDate.class);
			if(d != null) {
				try {
					f.setAccessible(true);
					_dRefreshDate = (Date) f.get(_obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void addCategories(String sName, String[] saCategories) {
		Set<String> categories = _categories.get(sName);
		if(categories == null) {
			categories = new HashSet<>();
			_categories.put(sName, categories);
		}
		categories.addAll(Arrays.asList(saCategories));
	}

	
	public boolean hasChanged(String... saFieldOrCategoryNames) {
		if(_values.size() == 0)
			return false;
		
		HashSet<String> fieldsOrCategories = saFieldOrCategoryNames == null || saFieldOrCategoryNames.length == 0 ? null : new HashSet<>(Arrays.asList(saFieldOrCategoryNames));
		for(Field f : _class.getDeclaredFields()) {
			ObserveValue a = f.getAnnotation(ObserveValue.class);
			if(a != null) {
				try {
					String sName = a.name().length() == 0 ? f.getName() : a.name();
					Set<String> categories = _categories.get(sName);
					if(fieldsOrCategories == null || fieldsOrCategories.contains(sName) || (categories != null && categories.stream().anyMatch(fieldsOrCategories::contains))) {
						Object o1 = _values.get(sName);
						Object o2 = f.get(_obj);
						if((o1 == null && o2 != null) || (o1 != null && !o1.equals(o2)))
							return true;
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return false;
	}


	public Date getRefreshDate() {
		return _dRefreshDate;
	}

}
