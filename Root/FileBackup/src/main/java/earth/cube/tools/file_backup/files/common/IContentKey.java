package earth.cube.tools.file_backup.files.common;

public interface IContentKey {
	
	String getSha256();
	
	long getSize();
	
	String getExtension();

}
