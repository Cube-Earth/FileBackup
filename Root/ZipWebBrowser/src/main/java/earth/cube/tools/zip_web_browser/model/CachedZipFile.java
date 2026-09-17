package earth.cube.tools.zip_web_browser.model;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import earth.cube.tools.zip_web_browser.flattened_directory.FlatRoot;
import lombok.Getter;
import lombok.Setter;

public class CachedZipFile extends FlatRoot {
	
	@Getter @Setter
	protected FileUniqueId _uniqueId;
	
	

	public CachedZipFile() {
	}

	
	public void addEntry(ZipEntry e) {
		
		String sPath = e.getName();
		addEntry(sPath, sPath.endsWith("/")).init( entry -> {
			entry.setSize(e.getSize());
			entry.setLastModified(new Date(e.getLastModifiedTime().toMillis()));
		});

	}
	
	public boolean updateFromZip(ZipFile zip) throws IOException {
		FileUniqueId uniqueId = new FileUniqueId(new File(zip.getName()));
		if(uniqueId.equals(_uniqueId))
			return false;
		
		for(Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements(); ) {
			ZipEntry entry = en.nextElement();
			addEntry(entry);
		}
		
		return true;
	}


}
