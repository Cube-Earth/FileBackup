package earth.cube.tools.zip_web_browser.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class FileUniqueId {
	
	@JsonProperty("size")
	private long _nSize;

	@JsonProperty("lastModified")
	private Date _dLastModified;

	@JsonProperty("inode")
	private long _nInodeId;

	
	public FileUniqueId(File file) throws IOException {
		Map<String, Object> _attrs = Files.readAttributes(file.toPath(), "unix:lastModifiedTime,ino,size,ctime");
		_nSize = (Long) _attrs.get("size");
		_dLastModified = Date.from(((FileTime) _attrs.get("lastModifiedTime")).toInstant());
		_nInodeId = (Long) _attrs.get("ino");
	}
	
}
