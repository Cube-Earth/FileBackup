package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BeanGetterTest {

	public static class A {
		protected long _v;
		
		public long getValue() {
			return _v;
		}
		
	}
	
	
	@Test
	public void test_1() throws Exception {
		BeanGetter getter = new BeanGetter(A.class.getDeclaredMethod("getValue"));
		A a = new A();
		assertEquals(AttributeType.LONG, getter.getType());
		
		assertEquals(0L, getter.get(a));
		a._v = 7;
		assertEquals(7L, getter.get(a));
		
		
	}
	
}
