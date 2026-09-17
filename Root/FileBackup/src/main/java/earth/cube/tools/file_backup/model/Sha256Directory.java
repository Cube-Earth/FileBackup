package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class Sha256Directory {

//	private MessageDigest _digest = MessageDigest.getInstance("SHA-256");
	
	public void add(LinuxFile file) {
/*		
		LinuxInode inode = file.getInode();
		String s = String.format("", inode.getSize(), inode.getSha256(), file.getFile().getName());
		_digest.update(b, 0, n);
	}
	
	try(FileInputStream fis = new FileInputStream(file)) {
		byte[] b = new byte[1024];
		int n = 0; 
		    
		while ((n = fis.read(b)) != -1) {
			digest.update(b, 0, n);
		}
		
		return toHexString(digest.digest());
*/
	}
}

