package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.object_mapper.annotations.AfterLoading;
import earth.cube.tools.file_backup.object_mapper.annotations.Attribute;

public class BeanClassTest {
	
	protected final static Object IS_IGNORED = new Object();
	
	public static void assertSetEquals(Set<String> actual, String... saExpected) {
		List<String> expected = Arrays.asList(saExpected);
		Set<String> values = new HashSet<>(actual);
		values.removeAll(expected);
		if(values.size() > 0)
			fail("Unexpected values: " + String.join(",", values));
		values = new HashSet<>(expected);
		values.removeAll(actual);
		if(values.size() > 0)
			fail("Missing values: " + String.join(",", values));
	}

	
	public static class BeanClassX<T> extends BeanClass<T> {

		public BeanClassX(Class<T> clazz, boolean bRaiseExceptions) throws Exception {
			super(clazz, bRaiseExceptions);
		}

		public BeanClassX(Class<T> clazz, String sScope, boolean bRaiseExceptions) throws Exception {
			super(clazz, sScope, bRaiseExceptions);
		}
		
		public void verifySetters(String... saExpectedSetters) {
			assertSetEquals(_setters.keySet(), saExpectedSetters);
		}
		
		public void verifyGetters(String... saExpectedGetters) {
			assertSetEquals(_getters.keySet(), saExpectedGetters);
		}
	}
	
	
	
	public static class A {
		
		private boolean _bDirtyDocument = true;
		
		protected int count = 7;
		
		protected int count2 = 3;
		
		@Attribute(name="g1")
		protected float _f1 = 0.5f;
		
		@Attribute(name="g2", scope="a")
		@Attribute(name="h2", scope="b")
		protected float _f2 = 1.75f;
		
		public int getCount2() {
			return 2;
		}
		
		@Attribute(name="h2", scope="b")
		@Attribute(name="g2", scope="c")
		public float getFloat() {
			return 2.5f;
		}
		
		
		@Attribute(name="g2", scope="a")
		@Attribute(name="h2", scope="c")
		public void setFloat(float f) {
			_f1 = f+1;
		}
		
		@AfterLoading()
		@AfterLoading(scope="a")
		private void after1() {
			count += count2 / 10;
		}
			
		@AfterLoading()
		@AfterLoading(scope="b")
		private void after2(String sScope) {
			if(sScope.equals(""))
				count += count2 / 20;
			else
				if(sScope == "b")
					count += count2 / 30;
				else
					throw new IllegalArgumentException(sScope);
		}
	}
	
	
	@Test
	public void test_get_1_no_scope() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, false);
		
		c.verifyGetters("dirtyDocument", "dirtydocument", "count", "count2", "g1", "f2", "float");

		assertEquals(true, c.getValue(a, "dirtyDocument"));
		assertEquals(7, c.getValue(a, "count"));
		assertEquals(2, c.getValue(a, "count2"));
		
		assertEquals(null, c.getValue(a, "f1"));
		assertEquals(0.5f, c.getValue(a, "g1"));
		
		assertEquals(1.75f, c.getValue(a, "f2"));
		assertEquals(null, c.getValue(a, "g2"));
		assertEquals(null, c.getValue(a, "h2"));
		assertEquals(2.5f, c.getValue(a, "float"));
	}

	@Test
	public void test_set_1_no_scope() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, false);

		c.verifySetters("dirtyDocument", "dirtydocument", "count", "count2", "g1", "f2", "float");
		
		c.setValue(a, "dirtyDocument", false);
		c.setValue(a, "count", 8);
		c.setValue(a, "count2", 9);
		
		c.setValue(a, "f1", IS_IGNORED);
		assertEquals(0.5f, a._f1);

		c.setValue(a, "g1", 2.2f);
		assertEquals(2.2f, a._f1);

		c.setValue(a, "f2", 4.33f);
		c.setValue(a, "g2", IS_IGNORED);
		c.setValue(a, "h2", IS_IGNORED);
		c.setValue(a, "float", 2.6f);
		
		assertEquals(false, a._bDirtyDocument);
		assertEquals(8, a.count);
		assertEquals(9, a.count2);

		assertEquals(3.6f, a._f1);
		assertEquals(4.33f, a._f2);
	}
	

	@Test
	public void test_set_1_ignored() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, false);

		c.setValue(a, "f1", IS_IGNORED);
		assertEquals(0.5f, a._f1);

		c.setValue(a, "f1", 2.2f);
		assertEquals(0.5f, a._f1);

		assertThrows(IllegalArgumentException.class, () -> c.setValue(a, "g1", IS_IGNORED));

		c.setValue(a, "g1", 2.2f);
		assertEquals(2.2f, a._f1);		
	}
	
	@Test
	public void test_get_1_scope_a() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "a", false);
		
		c.verifyGetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "g2", "float");

		assertEquals(true, c.getValue(a, "dirtyDocument"));
		assertEquals(7, c.getValue(a, "count"));
		assertEquals(2, c.getValue(a, "count2"));
		
		assertEquals(0.5f, c.getValue(a, "f1"));
		assertEquals(null, c.getValue(a, "g1"));
		
		assertEquals(1.75f, c.getValue(a, "g2"));
		assertEquals(2.5f, c.getValue(a, "float"));
		assertEquals(null, c.getValue(a, "h2"));
	}
	
	@Test
	public void test_set_1_scope_a() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "a", false);

		c.verifySetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "g2");
		
		c.setValue(a, "dirtyDocument", false);
		c.setValue(a, "count", 8);
		c.setValue(a, "count2", 9);
		
		c.setValue(a, "f1", 2.2f);
		assertEquals(2.2f, a._f1);
		c.setValue(a, "g1", IS_IGNORED);

		c.setValue(a, "f2", IS_IGNORED);
		c.setValue(a, "g2", 4.33f);
		c.setValue(a, "h2", IS_IGNORED);
		c.setValue(a, "float", IS_IGNORED);
		
		assertEquals(false, a._bDirtyDocument);
		assertEquals(8, a.count);
		assertEquals(9, a.count2);

		assertEquals(5.33f, a._f1);
		assertEquals(1.75f, a._f2);
	}
	
	@Test
	public void test_get_1_scope_b() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "b", false);
		
		c.verifyGetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "h2");
		
		assertEquals(2.5f, c.getValue(a, "h2"));
	}
	
	@Test
	public void test_set_1_scope_b() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "b", false);

		c.verifySetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "h2", "float");
				
		c.setValue(a, "f1", 2.2f);
		c.setValue(a, "g1", IS_IGNORED);
		assertEquals(2.2f, a._f1);

		c.setValue(a, "f2", IS_IGNORED);
		c.setValue(a, "g2", IS_IGNORED);
		c.setValue(a, "h2", 4.33f);
		
		c.setValue(a, "float", 3.1f);

		assertEquals(4.33f, a._f2);
		assertEquals(4.1f, a._f1);
	}
	
	
	@Test
	public void test_get_1_scope_c() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "c", false);
		
		c.verifyGetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "f2", "g2");
		
		assertEquals(2.5f, c.getValue(a, "g2"));
	}
	
	@Test
	public void test_set_1_scope_c() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "c", false);

		c.verifySetters("dirtyDocument", "dirtydocument", "count", "count2", "f1", "f2", "h2");
				
		c.setValue(a, "f1", 2.2f);
		c.setValue(a, "g1", IS_IGNORED);
		assertEquals(2.2f, a._f1);

		c.setValue(a, "f2", 1.7f);
		assertEquals(1.7f, a._f2);
		
		c.setValue(a, "g2", IS_IGNORED);
		c.setValue(a, "h2", 4.33f);
		
		assertEquals(5.33f, a._f1);
	}

	@Test
	public void test_get_1_raiseException() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, true);
		assertThrows(IllegalStateException.class, () -> { c.getValue(a, "f1"); });
	}

	@Test
	public void test_set_1_raiseException() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, true);
		assertThrows(IllegalStateException.class, () -> { c.setValue(a, "f1", true); });
	}

	
	@Test
	public void test_executeAfterLoading_1() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, false);
		a.count2 = 60;
		c.executeAfterLoading(a);
		assertEquals(7 + 6 + 3, a.count);
	}

	@Test
	public void test_executeAfterLoading_2() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "a", false);
		a.count2 = 60;
		c.executeAfterLoading(a);
		assertEquals(7 + 6, a.count);
	}

	@Test
	public void test_executeAfterLoading_3() throws Exception {
		A a = new A();
		BeanClassX<A> c = new BeanClassX<>(A.class, "b", false);
		a.count2 = 60;
		c.executeAfterLoading(a);
		assertEquals(7 + 2, a.count);
	}
}
