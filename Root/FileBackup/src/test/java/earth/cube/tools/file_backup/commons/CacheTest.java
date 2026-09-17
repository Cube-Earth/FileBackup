package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CacheTest {
	
	protected static class TestCache extends Cache<String,String> {
		
		public TestCache() {
			_nExpiryInterval = 500;
			_nMaxEntries = 5;
		}
		
		
		public String dumpCacheKeys() {
			List<CacheEntry<String,String>> entries = new ArrayList<>(_map.values());
			entries.sort(new Comparator<CacheEntry<String,String>>() {

				@Override
				public int compare(CacheEntry<String,String> o1, CacheEntry<String,String> o2) {
					return Long.compare(o1.accessed, o2.accessed);
				}
				
			});
			
			return String.join(",", new Iterable<String>() {

				@Override
				public Iterator<String> iterator() {
					return entries.stream().map( c -> "" + c.key ).iterator();
				}
				
			});
					
		}
		
	}
	
	
	@Test
	public void test_1() throws InterruptedException {
		TestCache c = new TestCache();
		assertEquals("", c.dumpCacheKeys());
		
		assertEquals("1", c.get("a", k -> "1"));
		Thread.sleep(50);
		assertEquals("2", c.get("b", k -> "2"));
		Thread.sleep(50);
		assertEquals("3", c.get("c", k -> "3"));
		Thread.sleep(50);
		assertEquals("2", c.get("b", k -> "4"));
		assertEquals("2", c.get("b", k -> "5"));
		Thread.sleep(150);
		assertEquals("a,c,b", c.dumpCacheKeys());
		assertEquals("6", c.get("d", k -> "6"));
		Thread.sleep(50);
		assertEquals("7", c.get("e", k -> "7"));
		Thread.sleep(50);
		assertEquals("a,c,b,d,e", c.dumpCacheKeys());
		assertEquals("8", c.get("f", k -> "8"));
		assertEquals("c,b,d,e,f", c.dumpCacheKeys());
		Thread.sleep(210);
		c.purge();
		assertEquals("b,d,e,f", c.dumpCacheKeys());
		Thread.sleep(50);
		assertEquals("b,d,e,f", c.dumpCacheKeys());
		assertEquals("8", c.get("f", k -> "9"));
		Thread.sleep(50);
		assertEquals("b,d,e,f", c.dumpCacheKeys());
		assertEquals("10", c.get("a", k -> "10"));
		assertEquals("d,e,f,a", c.dumpCacheKeys());
		Thread.sleep(110);
		c.purge();
		assertEquals("e,f,a", c.dumpCacheKeys());
	}

}
