package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestUtilTest {
	
	@Test
	public void test_abc_1() {
		assertEquals("test_abc_1", TestUtil.getMethodName());
	}

	
	protected String getMethodName() {
		return TestUtil.getMethodName(2);
	}
	
	@Test
	public void test_abc_2() {
		assertEquals("test_abc_2", getMethodName());
	}

}
