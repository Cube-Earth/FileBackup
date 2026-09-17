package earth.cube.tools.zip_web_browser;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import earth.cube.tools.zip_web_browser.model.CachedZipFile;
import earth.cube.tools.zip_web_browser.utils.ResponseBuilder;

public class ZipBrowser implements Closeable {
	

	
	protected File _zipFile;
	protected File _zipCacheFile;
	private ZipFile _zip;
	private CachedZipFile _zipCache;
	
	
	public ZipBrowser(File file) throws IOException {
		_zipFile = file;
		_zip = new ZipFile(file);
		_zipCacheFile = new File(file.getAbsolutePath() + ".cache");
		readZipCache();
	}
	
	
	protected void readZipCache() throws StreamReadException, DatabindException, IOException {
		ObjectMapper om = new ObjectMapper();
		if(_zipCacheFile.exists())
			_zipCache = om.readValue(_zipCacheFile, CachedZipFile.class);
		else
			_zipCache = new CachedZipFile();
		if(_zipCache.updateFromZip(_zip))
			om.writeValue(_zipCacheFile, _zipCache);
	}

	@Override
	public void close() throws IOException {
		_zip.close();
	}
	
	
	public void render(String sPath, HttpServletResponse response) throws IOException {
		try {
			_zipCache.renderEntry(sPath, sHtml -> {
				ResponseBuilder.from(response).html(sHtml).send();
			}, entry -> {
				ZipEntry e = _zip.getEntry(sPath);
				ResponseBuilder.from(response).contentTypeByPath(sPath)
					.length((int) e.getSize())
					.body(_zip.getInputStream(e), false)
					.send();
			});
		}
		catch(Throwable t) {
			ResponseBuilder.from(response).sendError(t);
		}
	}

}
