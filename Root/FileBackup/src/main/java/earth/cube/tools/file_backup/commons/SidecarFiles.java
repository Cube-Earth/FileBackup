package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SidecarFiles {

	// TODO: extend by hc2 (ios live image file extension), canon raw and others	
	public final static Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg"));
	
	
	public SidecarFiles() {
	}
	
	
	public static Set<File> findSafeSidecars(Set<File> files, File baseFile) throws IOException {
		File file;
		File parent = baseFile.getParentFile();
		if(files == null)
			files = new HashSet<>();
		
		// MacOS xattr files
		file = new File(parent, "._" + baseFile.getName());
		if(file.exists() && CloseableAction.execute(new XattrFile(file), f -> f.isValid())) {
			files.add(file);
		}
		
		// XMP metadata files (for image files)
		String sExt = FileUtil.getExtension(baseFile);
	
		if(IMAGE_EXTENSIONS.contains(sExt)) {
			file = FileUtil.findFileByName(parent, FileUtil.getNameWithoutExtension(baseFile) + ".xmp");
			if(file != null) {
				files.add(file);
				findSafeSidecars(files, file);
			}
		}
		
		return files;
	}
	
	public static Set<File> findUnsafeSidecars(Set<File> files, File baseFile) throws IOException {
		File file;
		File parent = baseFile.getParentFile();
		if(files == null)
			files = new HashSet<>();
		
		// MacOS xattr files
		file = new File(parent, "._" + baseFile.getName());
		if(file.exists() && CloseableAction.execute(new XattrFile(file), f -> f.isValid())) {
			files.add(file);
		}
		
		// XMP metadata files (for image files)
		String sExt = FileUtil.getExtension(baseFile);
	
		if(IMAGE_EXTENSIONS.contains(sExt)) {
			List<File> files2 = FileUtil.findFilesByName(parent, FileUtil.getNameWithoutExtension(baseFile) + ".xmp");
			for(File file2 : files2) {
				files.add(file2);
				findUnsafeSidecars(files, file2);
			}
		}
		
		return files;
	}

	public static boolean isSidecar(File file) throws IOException {
		File parent = file.getParentFile();
		String sName = file.getName();
		
		// MacOS xattr files
		if(sName.startsWith("._") && new File(parent, sName.substring(2)).exists() && 
				CloseableAction.execute(new XattrFile(file), f -> f.isValid())) {
			return true;
		}
		
		// XMP metadata files (for image files)
		String sExt = FileUtil.getExtension(file);
		if(sExt.equalsIgnoreCase(".xmp")) {
			List<File> files = FileUtil.findFilesByName(parent, FileUtil.getNameWithoutExtension(file), IMAGE_EXTENSIONS);
			if(files.size() > 0) {
				return true;
			}
		}
		
		return false;
	}
	

}
