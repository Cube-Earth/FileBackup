package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtil {
	
	public static String getRawExtension(File file) {
		String s = file.getName();
		int i = s.lastIndexOf('.');
		return i == -1 ? "" : s.substring(i);		
	}

	public static String getExtension(File file) {
		String s = file.getName();
		int i = s.lastIndexOf('.');
		return i == -1 ? "" : s.substring(i).toLowerCase();		
	}
	
	public static String getRawExtension(String sPath) {
		String s = getName(sPath);
		int i = s.lastIndexOf('.');
		return i == -1 ? "" : s.substring(i);		
	}


	public static String getNameWithoutExtension(File file) {
		String s = file.getName();
		int i = s.lastIndexOf('.');
		return i == -1 ? s : s.substring(0, i);		
	}
	
	
	private static String getName(String sPath) {
		int i = sPath.lastIndexOf('/');
		return i == -1 ? sPath : sPath.substring(i+1);	
	}

	public static String getNameWithoutExtension(String sPath) {
		String s = getName(sPath);
		int i = s.lastIndexOf('.');
		return i == -1 ? s : s.substring(0, i);	
	}
	
	
	public static boolean deleteDirectory(File dir) {
    	if(dir.isDirectory())
    		for (File file : dir.listFiles()) {
        		deleteDirectory(file);
    		}
	    return dir.delete();
	}
	
	public static int countFiles(File dir) {
		int n = 0;
        for (File file : dir.listFiles()) {
        	if(file.isDirectory())
        		n += countFiles(file);
        	else {
        		if(!file.getName().equals(".DS_Store"))
        			n++;
        	}
        }
	    return n;
	}
	
	public static byte[] read(File file) throws IOException {
		if(!file.exists())
			return null;
		byte[] buf = new byte[(int) file.length()];
		try(FileInputStream is = new FileInputStream(file)) {
			int n = is.read(buf);
			if(n != buf.length)
				throw new IllegalStateException("Huh?");
		}
		return buf;
	}

	public static String readAsString(File file) throws IOException {
		return !file.exists() ? null : new String(read(file), StandardCharsets.UTF_8);
	}
	
	public static void write(File file, String sContent) throws FileNotFoundException, IOException {
		try(FileOutputStream os = new FileOutputStream(file)) {
			os.write(sContent.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	public static void write(File file, byte[] content) throws FileNotFoundException, IOException {
		try(FileOutputStream os = new FileOutputStream(file)) {
			os.write(content);
		}
	}
	
	public static void touch(File file) throws IOException {
		if(!file.exists())
			new FileOutputStream(file).close();
		else
			file.setLastModified(new Date().getTime());
	}
	
	public static File mkdirs(File dir) {
		if(!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("Could not create directory '" + dir.getAbsolutePath() + "'!");
		}
		return dir;
	}
	
	public static void renameTo(File src, File dst) {
		log.debug("renameTo: renaming file from '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'...");
		File parent = dst.getParentFile();
		if(parent != null) {
			parent.mkdirs();			
		}
		if(!src.renameTo(dst)) {
			throw new IllegalStateException("Could not rename file '" + src.getAbsolutePath() + "' to '" + dst.getAbsolutePath() + "'!");
		}
	}

	public static String getRelativePath(File baseDir, File file) {
		String s1 = baseDir.getAbsolutePath().replace('\\', '/');
		String s2 = file.getAbsolutePath().replace('\\', '/');
		return s2.startsWith(s1 + '/') ? s2.substring(s1.length() + 1) : s1.equals(s2) ? "" : null;
	}
	
	public static File relocate(File srcDir, File dstDir, File file) {
		String sRelPath = getRelativePath(srcDir, file);
		return sRelPath == null ? null : new File(dstDir, sRelPath);
	}
	
	public static File relocate(File srcDir, File dstDir, File file, boolean bCreateDirs) {
		File dstFile = relocate(srcDir, dstDir, file);
		if(dstFile != null && bCreateDirs)
			dstFile.getParentFile().mkdirs();
		return dstFile;
	}


	public static String getParentDirectory(String sPath) {
		if(sPath == null || sPath.length() == 0 || sPath.equals("/"))
			return null;
		
		int i = sPath.lastIndexOf('/');
		if(i == -1 || i == 0)
			return "";
		return sPath.substring(0, i);
	}	


	public static List<String> getAllParentDirectories(String sPath, boolean bIncludeInitialPath) {
		List<String> dirs = new ArrayList<>();
		if(bIncludeInitialPath) {
			dirs.add(sPath);
		}

		sPath = getParentDirectory(sPath);
		while(sPath != null) {
			dirs.add(sPath);
			sPath = getParentDirectory(sPath);
		}
		
		return dirs;
	}
	
	public static List<File> findFilesByName(File parent, String sNameToSearch) {
		File[] candidates = parent.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String sName) {
				return sName.equalsIgnoreCase(sNameToSearch);
			}
			
		});
		
		return Arrays.asList(candidates);
	}

	public static List<File> findFilesByName(File parent, String sNameWithouExt, Collection<String> exts) {
		File[] candidates = parent.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String sName) {
				for(String sExt : exts) {
					return sName.equalsIgnoreCase(sNameWithouExt + sExt);
				}
				return false;
			}
			
		});
		
		return Arrays.asList(candidates);
	}

	public static List<File> findFilesByNameWithouExtension(File parent, String sNameToSearch) {
		File[] candidates = parent.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String sName) {
				return FileUtil.getNameWithoutExtension(sName).equalsIgnoreCase(sNameToSearch);
			}
			
		});
		
		return Arrays.asList(candidates);
	}

	public static List<File> findSiblings(File baseFile) {
		String sNameToSearch = FileUtil.getNameWithoutExtension(baseFile);
		File[] candidates = baseFile.getParentFile().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String sName) {
				return FileUtil.getNameWithoutExtension(sName).equalsIgnoreCase(sNameToSearch);
			}
			
		});
		
		return Arrays.asList(candidates);
	}

	public static File findFileByName(File parent, String sNameToSearch) {
		List<File> files = findFilesByName(parent, sNameToSearch);
		return files.size() == 1 ? files.get(0) : null;
	}

	public static File findFileByName(File parent, String sNameWithoutExt, Collection<String> exts) {
		List<File> files = findFilesByName(parent, sNameWithoutExt, exts);
		return files.size() == 1 ? files.get(0) : null;
	}

	public static String getRootDirectoryName(String sFilePath) {
		String[] saPath = sFilePath == null ? null : sFilePath.split("/", 3);
		if(saPath == null) {
			return null;
		}
		if(saPath.length == 3 && saPath[0].length() == 0) {
			return saPath[1];
		}
		if(saPath.length >= 2 && saPath[0].length() != 0) {
			return saPath[0];
		}
		
		return "";
	}

	public static void deleteFile(File file) {
		if(file.exists()) {
			if(!file.delete())
				throw new IllegalStateException("Could not delete file '" + file.getAbsolutePath() + "'!");
		}
	}
	
	
	

}
