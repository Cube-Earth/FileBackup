package earth.cube.tools.zip_web_browser.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import earth.cube.tools.zip_web_browser.flattened_directory.FlatRoot;
import earth.cube.tools.zip_web_browser.utils.FileUtil;

public class CondensedDirectory extends FlatRoot {
	
	public CondensedDirectory(File dir) {
		try {
			readFiles(dir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void addFile(String sPath, File zip) {
		
		addFile(sPath).init( entry -> {
			entry.setSize(zip.length());
			entry.setLastModified(new Date(zip.lastModified()));
			entry.setData(zip);
		});

	}	

	private void readFiles(File dir) throws IOException {
		for(File file : dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".list");
			}})) {
			
			readFile(file);
		}
	}

	private void readFile(File file) throws IOException {
		String sContent = FileUtil.readAsString(file);
		Set<String> set = new LinkedHashSet<>();
		for(String s : sContent.split("\n"))
			set.add(s.endsWith("\r") ? s.substring(0, s.length()-1) : s);
		List<String> list = new ArrayList<>(set);
		Collections.sort(list);
		String sNewContent = String.join("\n", list);
		if(!sContent.equals(sNewContent))
			FileUtil.write(file, sNewContent);
		
		String sName = file.getName().replace('\\', '/');
		for(String sPath : list) {
			File entry = new File(sPath);
			addFile(sName + '/' + entry.getName(), entry);
		}
		
	}
	
}
