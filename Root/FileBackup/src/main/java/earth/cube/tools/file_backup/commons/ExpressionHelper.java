package earth.cube.tools.file_backup.commons;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExpressionHelper {
	
	
	public static <T> void executeIfNotNull(T obj, Consumer<T> funcNotNull, Consumer<T> funcNull) {
		if(obj != null) {
			if(funcNotNull != null)
				funcNotNull.accept(obj);
		}
		else {
			if(funcNull != null)
				funcNull.accept(obj);
		}
	}

	public static <T, U> U evaluateIfNotNull(T obj, Function<T, U> funcNotNull, Function<T, U> funcNull) {
		if(obj != null) {
			if(funcNotNull != null)
				return funcNotNull.apply(obj);
		}
		else {
			if(funcNull != null)
				return funcNull.apply(obj);
		}
		return null;
	}

	public static <T, U> U addItem(Map<T, U> map, T key, Function<T, U> funcNew) {
		U value = map.get(key);
		if(value == null) {
			value = funcNew.apply(key);
			map.put(key, value);
		}
		return value;
	}
}
