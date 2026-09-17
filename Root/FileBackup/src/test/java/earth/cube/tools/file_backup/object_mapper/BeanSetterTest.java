package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BeanSetterTest {

	public static class A {
		protected String _s;
		
		public void setValue1(String s) {
			_s = s;
		}
		
	}
	
	
	@Test
	public void test_1() throws Exception {
		BeanSetter setter = new BeanSetter(A.class.getDeclaredMethod("setValue1", String.class));
		A a = new A();
		assertEquals(AttributeType.STRING, setter.getType());
		
		assertEquals(null, a._s);
		setter.set(a, "test");
		assertEquals("test", a._s);
		
		
	}
	
}
