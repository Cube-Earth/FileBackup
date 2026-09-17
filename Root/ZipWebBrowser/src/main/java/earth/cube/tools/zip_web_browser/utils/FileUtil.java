package earth.cube.tools.zip_web_browser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtil {
	
	public static String readAsString(File file) throws IOException {
		byte[] buf = new byte[(int) file.length()];
		try(FileInputStream is = new FileInputStream(file)) {
			int n = is.read(buf);
			if(n != buf.length)
				throw new IllegalStateException("Huh?");
		}
		return new String(buf, StandardCharsets.UTF_8);
	}
	
	public static void write(File file, String sContent) throws FileNotFoundException, IOException {
		try(FileOutputStream os = new FileOutputStream(file)) {
			os.write(sContent.getBytes(StandardCharsets.UTF_8));
		}
	}

	
	public static String getFileExtension(String sPath) {
		int i = sPath.lastIndexOf('.');
		return i == -1 ? "" : sPath.substring(i + 1).toLowerCase();
	}

	
}
