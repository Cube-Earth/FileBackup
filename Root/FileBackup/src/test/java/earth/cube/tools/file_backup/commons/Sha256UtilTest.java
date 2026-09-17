package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

public class Sha256UtilTest {

	@Test
	public void test_toHexString_1() throws UnsupportedEncodingException {
		assertEquals("4131", Sha256Util.toHexString(new String("A1").getBytes("iso-8859-1")));
	}
	
	@Test
	public void test_toHexString_2() throws UnsupportedEncodingException {
		assertEquals("1eb0", Sha256Util.toHexString(new byte[] { 0x1e, (byte) 0xb0 }));
	}
}
