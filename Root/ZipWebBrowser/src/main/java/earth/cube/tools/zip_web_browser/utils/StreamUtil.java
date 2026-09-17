package earth.cube.tools.zip_web_browser.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil {
	
	public static void spool(InputStream is, boolean bCloseIn, OutputStream os, boolean bCloseOut) throws IOException {
		byte[] buf = new byte[0x4000];
		
		try {
			int n = is.read(buf);
			while(n != -1) {
				os.write(buf, 0, n);
				n = is.read(buf);
			}
		}
		catch(IOException e) {
			if(bCloseIn) {
				try {
					is.close();
				} catch (IOException e1) {
				}
			}
			if(bCloseOut) {
				try {
					os.close();
				} catch (IOException e1) {
				}
			}
			throw e;
		}
	}

}
