package earth.cube.tools.file_backup.commons;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Cache<K, V> {
	
	protected long _nExpiryInterval = 1000*60*5;  // 5 min
	protected int _nMaxEntries = 10000;
	
	
	protected static class CacheEntry<K,V> {
		K key;
		long accessed = System.currentTimeMillis();
		V value;
		
		public CacheEntry(K k, V v) {
			key = k;
			value = v;
		}
	}

	protected static class PurgeData<K> {
		long oldestDate = Long.MAX_VALUE;
		K oldestKey = null;
		int deleted;
	}
	
	protected ConcurrentHashMap<K, CacheEntry<K,V>> _map = new ConcurrentHashMap<>();
	

	public void purge() {
		final PurgeData<K> d = new PurgeData<>();
		long nDue = System.currentTimeMillis() - _nExpiryInterval;
		new HashMap<>(_map).forEach( (k,v) -> {
			if(v.accessed < nDue) {
				_map.remove(v.key);
				d.deleted++;
			}
			else
				if(v.accessed < d.oldestDate) {
					d.oldestDate = v.accessed;
					d.oldestKey = v.key;
				}
		});
		if(d.deleted == 0 && _map.size() >= _nMaxEntries)
			_map.remove(d.oldestKey);
	}
	
	public V get(K key, Function<K,V> func) {
		CacheEntry<K,V> c = _map.computeIfAbsent(key, k -> {
			purge();
			return new CacheEntry<K,V>(k, func.apply(k));
		});
		c.accessed = System.currentTimeMillis();
		return c.value;
	}

	public void remove(K key) {
		_map.remove(key);
	}

	public void reset() {
		_map.clear();
	}
	


}
