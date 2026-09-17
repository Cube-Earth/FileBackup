package earth.cube.tools.file_backup.commons;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class HashWrapper<T,U extends Object> {
	
	@Getter
	protected T _hash;
	
	@Getter
	protected U _object;
	
	public HashWrapper(T hash) {
		_hash = hash;
	}
	
	@Override
	public int hashCode() {
		return _hash.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return _hash.equals(obj);
	}
	

	public static <T, U> List<U> unwrap(Set<HashWrapper<T, U>> set) {
		List<U> objects = set.stream().map(HashWrapper::getObject).collect(Collectors.toList());
		return objects;
	}
}
