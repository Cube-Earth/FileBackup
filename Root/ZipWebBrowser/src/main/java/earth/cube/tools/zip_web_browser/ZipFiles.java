package earth.cube.tools.zip_web_browser;

import java.io.File;

import earth.cube.tools.zip_web_browser.model.CondensedDirectory;

public class ZipFiles extends CondensedDirectory {
	
	public final static ZipFiles INSTANCE = new ZipFiles();

	public ZipFiles() {
		super(new File(Application.CONFIG.getProperty("zip.dir")));
	}

}
