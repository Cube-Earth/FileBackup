package earth.cube.tools.zip_web_browser.utils;

import java.io.IOException;

public interface Consumer<T> {
	
	void accept(T param) throws IOException;

}
