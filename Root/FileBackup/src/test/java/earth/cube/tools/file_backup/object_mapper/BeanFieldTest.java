package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BeanFieldTest {

	public static class A {
		protected int _a;
	}
	
	
	@Test
	public void test_1() throws Exception {
		BeanField f = new BeanField(A.class.getDeclaredField("_a"));
		A a = new A();
		assertEquals(AttributeType.INTEGER, f.getType());
		
		assertEquals(0, a._a);
		assertEquals(0, f.get(a));
		f.set(a, 5);
		assertEquals(5, a._a);
		assertEquals(5, f.get(a));
		
		
	}
	
}
