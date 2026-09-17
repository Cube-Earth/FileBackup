package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.object_mapper.adapter.MapAdapter;
import earth.cube.tools.file_backup.object_mapper.annotations.AfterLoading;
import earth.cube.tools.file_backup.object_mapper.annotations.Attribute;

public class BeanBuilderAndBeanTest {
	
	public static class A {
		private String a = "abc";
		
		@Attribute(name="count")
		protected int _nCount = 2;
		
		@Attribute(name="count")
		@Attribute(name="amount", scope="alt")
		void setCount(int i) {
			_nCount = _nCount + i + 2;
		}
		
		@AfterLoading()
		void postProcess() {
			_nCount += 10;
		}
	}
	
	
	@Test
	public void test_adopt_1() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "def");
		map.put("count", 3);
		map.put("amount", 9);
		
		A a = BeanBuilder.create(MapAdapter.class, A.class).build().adopt(map);
		
		assertEquals("def", a.a);
		assertEquals(17, a._nCount);
	}

	@Test
	public void test_adopt_raiseException_1() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "def");
		map.put("count", 3);
		map.put("amount", 9);
		
		Bean<Map<String, Object>, A> bean = BeanBuilder.create(MapAdapter.class, A.class).raiseExceptions(true).build();
		assertThrows(IllegalStateException.class, () -> { bean.adopt(map); });
	}

	@Test
	public void test_adopt_limited_attributes_1() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "def");
		map.put("count", 3);
		map.put("amount", 9);
		
		A a = BeanBuilder.create(MapAdapter.class, A.class).attributes("count").build().adopt(map);
		
		assertEquals("abc", a.a);
		assertEquals(17, a._nCount);
	}

	@Test
	public void test_adopt_scope_1() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "def");
		map.put("count", 5);
		map.put("amount", 9);
		
		A a = BeanBuilder.create(MapAdapter.class, A.class).scope("alt").build().adopt(map);
		
		assertEquals("def", a.a);
		assertEquals(16, a._nCount);
	}

	@Test
	public void test_adopt_scope_2() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "def");
		map.put("amount", 9);
		map.put("count", 5);
		
		A a = BeanBuilder.create(MapAdapter.class, A.class).scope("alt").build().adopt(map);
		
		assertEquals("def", a.a);
		assertEquals(5, a._nCount);
	}

}
