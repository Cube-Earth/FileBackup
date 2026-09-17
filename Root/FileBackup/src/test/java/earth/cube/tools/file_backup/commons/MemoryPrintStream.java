package earth.cube.tools.file_backup.commons;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MemoryPrintStream extends PrintStream {

	public MemoryPrintStream() throws UnsupportedEncodingException {
		super(new ByteArrayOutputStream(), false, StandardCharsets.UTF_8.toString());
	}
	
	
	public String getString() {
		flush();
		return new String(((ByteArrayOutputStream) out).toByteArray(), StandardCharsets.UTF_8);
	}

}
