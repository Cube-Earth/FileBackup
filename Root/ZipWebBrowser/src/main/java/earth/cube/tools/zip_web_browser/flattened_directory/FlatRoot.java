package earth.cube.tools.zip_web_browser.flattened_directory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import earth.cube.tools.zip_web_browser.utils.Consumer;
import earth.cube.tools.zip_web_browser.utils.Escaper;

public class FlatRoot {
	
	public final static String ROOT = "";
	
	protected Map<String,FlatEntry> _entries = new HashMap<>();
	
	{
		_entries.put(ROOT, new FlatDirectory(new File("")));
	}

	public FlatDirectory addDirectory(String sDir) {
		if(sDir == null || sDir.length() == 0 || sDir.equals("/"))
			return (FlatDirectory) _entries.get(ROOT);

		File path = new File(sDir);
		
		FlatDirectory dir = (FlatDirectory) _entries.get(sDir);
		if(dir == null) {
			dir = new FlatDirectory(path);
			_entries.put(sDir, dir);

			FlatDirectory parentDir = addDirectory(path.getParent());
			if(parentDir != null)
				parentDir.addDirectory(dir);
		}
		return dir;
	}

	
	public FlatEntry addFile(String sPath) {
		File path = new File(sPath);
		FlatEntry file = new FlatEntry(path);
		
		_entries.put(sPath, file);
		
		FlatDirectory flatDir = addDirectory(path.getParent());
		flatDir.addFile(file);
		return file;
	 }
	
	
	@SuppressWarnings("unchecked")
	public <T extends FlatEntry> T getEntry(String sPath) {
		return (T) _entries.get(sPath);
	}

	public FlatDirectory getRoot() {
		return (FlatDirectory) _entries.get(ROOT);
	}

	@JsonProperty
	protected Collection<FlatEntry> getEntries() {
		return Collections.unmodifiableCollection(_entries.values());
	}
	
	@JsonProperty
	protected void setEntries(List<FlatEntry> entries) {
		_entries = new HashMap<>();
		for(FlatEntry e : entries) {
			_entries.put(e.getPath(), e);
		}
	}
	
	public FlatEntry addEntry(String sPath, boolean bDir) {
		if(sPath.endsWith("/")) {
			sPath = sPath.substring(0, sPath.length()-1);
		}
		return bDir ? addDirectory(sPath) : addFile(sPath);
	}

	protected String renderDirectory(FlatDirectory dir) {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><body>");
		sb.append(String.format("<h1>%s</h1>", Escaper.escapeHtml(dir.getPath())));
	
		sb.append("<table>");
		
		String sParent = new File(dir.getPath()).getParent();
		sb.append(String.format("<tr><td><a href=\"%s\">..</a></td><td></td><td></td></tr>", Escaper.escapeHtmlString(sParent)));
		
		for(FlatDirectory subDir : dir.getDirectories()) {
			sb.append(subDir.getHtml());
		}

		for(FlatEntry file : dir.getDirectories()) {
			sb.append(file.getHtml());
		}

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
	public void renderEntry(String sPath, Consumer<String> htmlFunc, Consumer<FlatEntry> fileFunc) throws IOException {
		FlatEntry entry = _entries.get(sPath);
		if(entry == null)
			throw new FileNotFoundException(sPath);
		if(entry.isDirectory()) {
			htmlFunc.accept(renderDirectory(getRoot()));
		}
		else {
			fileFunc.accept(entry);
		}
	}
	

}
