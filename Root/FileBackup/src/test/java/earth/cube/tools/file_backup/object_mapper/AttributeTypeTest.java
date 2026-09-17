package earth.cube.tools.file_backup.object_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class AttributeTypeTest {

	@Test
	public void test_from_1() {
		assertEquals(AttributeType.BOOLEAN, AttributeType.from(Boolean.class));
		assertEquals(AttributeType.INTEGER, AttributeType.from(Integer.class));
		assertEquals(AttributeType.LONG, AttributeType.from(Long.class));
		assertEquals(AttributeType.DOUBLE, AttributeType.from(Float.class));
		assertEquals(AttributeType.DOUBLE, AttributeType.from(Double.class));
		assertEquals(AttributeType.STRING, AttributeType.from(String.class));
		assertEquals(AttributeType.DATE, AttributeType.from(Date.class));
		assertEquals(AttributeType.UNKNOWN, AttributeType.from(String[].class));
		assertEquals(AttributeType.UNKNOWN, AttributeType.from(Object.class));

		assertEquals(AttributeType.BOOLEAN, AttributeType.from(boolean.class));
		assertEquals(AttributeType.INTEGER, AttributeType.from(int.class));
		assertEquals(AttributeType.LONG, AttributeType.from(long.class));
		assertEquals(AttributeType.DOUBLE, AttributeType.from(float.class));
		assertEquals(AttributeType.DOUBLE, AttributeType.from(double.class));
	}
	

}
