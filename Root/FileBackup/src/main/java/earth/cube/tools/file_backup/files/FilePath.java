package earth.cube.tools.file_backup.files;

import java.io.File;

import earth.cube.tools.file_backup.commons.FileUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FilePath {
	
	@Getter
	protected FileSystem _fileSystem;
	
	@Getter
	protected String _sPath;
	
	
	public File getFile() {
		return new File(_fileSystem.getBaseDirectory(), _sPath);
	}
	
	private static String normalizePath(String sPath) {
		sPath = sPath.replace('\\', '/');
		if(sPath.startsWith("/")) {
			sPath = sPath.substring(1);
		}
		if(sPath.endsWith("/")) {
			sPath = sPath.substring(0, sPath.length()-1);
		}
		return sPath;
	}

	private static boolean isPathSafe(String sPath) {
		return !('/' + sPath + '/').matches("//|/../|/./");
	}
	
	private static boolean isPathSafe2(FileSystem fs, String sPath) {
		String s1 = fs.getBaseDirectory().getAbsolutePath().replace('\\', '/') + '/' + sPath;
		String s2 = new File(fs.getBaseDirectory(), sPath).getAbsolutePath().replace('\\', '/');
		return s1.equals(s2);
	}
	
	public static FilePath from(FileSystem fs, File file) {
		String sPath = FileUtil.getRelativePath(fs.getBaseDirectory(), file);
		if(sPath == null)
			throw new IllegalStateException(String.format("'%s' is not underneath '%s'!", file.getAbsolutePath(), fs.getBaseDirectory().getAbsolutePath()));
		return new FilePath(fs, sPath);
	}
	
	public static FilePath from(FileSystem fs, final String sPath) {
		String sTransformedPath;
		if(sPath == null || sPath.length() == 0 || sPath.equals("/") || sPath.equals("\\")) {
			sTransformedPath = null;
		}
		else {
			sTransformedPath = normalizePath(sPath);
			if(!isPathSafe(sTransformedPath)) {
				throw new IllegalStateException("Suspicious file path: " + sPath);
			}
			if(!isPathSafe2(fs, sTransformedPath)) {
				throw new IllegalStateException("Suspicious file path: " + sPath);
			}
		}
		return new FilePath(fs, sTransformedPath);
	}


}
