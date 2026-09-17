package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Sha256Util {
	
	
	public static long _nCalculatedChecksumBytes = 0;
	

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < b.length; i++) {
			String s = Integer.toHexString(b[i] & 0xFF);
			if(s.length() == 1)
				s = '0' + s;
			sb.append(s);
		}
		return sb.toString();		
	}
	
	public static String getFileExension(File file) {
		String s = file.getName();
		int i = s.lastIndexOf('.');
		return i == 0 ? "" : s.substring(i).toLowerCase();
	}
	
	public static boolean equalFiles(File file1, File file2, boolean bParanoia) throws FileNotFoundException, IOException {
		if(!file1.isFile() || !file2.isFile())
			throw new IllegalStateException("Huh?");
		if(file1.length() != file2.length())
			return false;
		
		if(!getFileExension(file1).equals(getFileExension(file2)))
			return false;
		
		if(!bParanoia)
			return true;
		
		byte[] b1 = new byte[4096];
		byte[] b2 = new byte[4096];
		try(FileInputStream is1 = new FileInputStream(file1)) {
			try(FileInputStream is2 = new FileInputStream(file2)) {
				int n1 = is1.read(b1);
				int n2 = is2.read(b2);
				while(n1 != 1 && n2 != -1) {
					if(n1 != n2) {
						throw new IllegalStateException("Huh??");
					}
					if(!Arrays.equals(b1, b2)) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	public static void ensureFileEquality(File file1, File file2, boolean bParanoia) throws FileNotFoundException, IOException {
		if(!equalFiles(file1, file2, bParanoia))
			throw new IllegalStateException("Huh?");
	}
	
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
		_nCalculatedChecksumBytes  += file.length();
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] b = new byte[1024];
			int n = 0; 
			    
			while ((n = fis.read(b)) != -1) {
				digest.update(b, 0, n);
			}
			
			return toHexString(digest.digest());
		}
	}
	
	public static String getChecksum(File file) throws IOException {
		try {
			MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
			return getFileChecksum(shaDigest, file);
		}
		catch(NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	public static String getChecksum(byte[] buf) throws IOException {
		try {
			_nCalculatedChecksumBytes  += buf.length;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(buf, 0, buf.length);
			return toHexString(digest.digest());
		}
		catch(NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	

}
