package earth.cube.tools.file_backup.object_mapper.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MapAdapterTest {
	
	private SimpleDateFormat _df = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
	
	@Test
	public void test_1() throws Exception {
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("long", 3L);
		map.put("str", "def");
		map.put("b", true);
		map.put("int", 12);
		map.put("dt", _df.parse("2020-02-08 12:30:15"));
		map.put("d", 4.6d);
		MapAdapter a = new MapAdapter();
		a.setObject(map);
		
		assertEquals(Arrays.asList("long", "str", "b", "int", "dt", "d"), a.getDefaultAttributes());
		
		assertEquals(true, a.getBoolean("b"));
		assertEquals(12, a.getInt("int"));
		assertEquals(3L, a.getLong("long"));
		assertEquals(4.6d, a.getDouble("d"));
		assertEquals(_df.parse("2020-02-08 12:30:15"), a.getDate("dt"));
		assertEquals("def", a.getString("str"));
		
		assertEquals(null, a.getString("e"));
		assertThrows(ClassCastException.class, () -> a.getString("b"));
	}

}
