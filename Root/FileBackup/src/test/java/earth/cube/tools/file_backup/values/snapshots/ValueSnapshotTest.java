package earth.cube.tools.file_backup.values.snapshots;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.values.snapshots.ObserveValue;
import earth.cube.tools.file_backup.values.snapshots.ValueSnapshot;

public class ValueSnapshotTest {
	
	public static class A {
		
		@ObserveValue(name = "a", categories = { "c", "e" })
		protected String _a;
		
		@ObserveValue(name = "b")
		protected int _b;
		
		@ObserveValue(name = "c", categories = { "e" })
		protected boolean _c;
		
		@ObserveValue()
		protected String _d;

		protected String _e;
	}
	
	protected A _a = new A();
	
	protected void setA(String a, int b, boolean c, String d, String e) {
		_a._a = a;
		_a._b = b;
		_a._c = c;
		_a._d = d;
		_a._e = e;
	}
	
	
	@Test
	public void test_1() {
		setA("abc", 2, true, "def", "ghi");
		ValueSnapshot s = new ValueSnapshot(_a, A.class);
	
		assertThat(s._values, allOf(
				aMapWithSize(4),
                hasEntry("a", "abc"),
                hasEntry("b", (Object) 2),
                hasEntry("c", (Object) true),
                hasEntry("_d", "def")
		));
		
		assertFalse(s.hasChanged());

		setA("jkl", 2, true, "def", "ghi");
		assertTrue(s.hasChanged());
		assertTrue(s.hasChanged("a"));
		assertTrue(s.hasChanged("c"));
		assertFalse(s.hasChanged("b"));
		assertTrue(s.hasChanged("e"));
		assertTrue(s.hasChanged("b", "c"));
		
		// TODO check refresh date
	
	}
	

}
