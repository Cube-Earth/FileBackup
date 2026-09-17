package earth.cube.tools.zip_web_browser;

import java.io.File;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

public class ZipBrowserCache {
	
	protected final static ZipBrowserCache INSTANCE = new ZipBrowserCache();
	
	protected LoadingCache<String, ZipBrowser> _browsers = Caffeine.newBuilder()
		    .maximumSize(50)
		    .expireAfterAccess(Duration.ofMinutes(30))
		    .build(sPath -> new ZipBrowser(new File(sPath)));
	
	
	@SuppressWarnings("static-access")
	public static ZipBrowser get(String sPath) {
		return INSTANCE.get(sPath);
	}

}
