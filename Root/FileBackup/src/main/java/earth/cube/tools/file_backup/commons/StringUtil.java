package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class StringUtil {
	public static String join(Collection<?> coll, String sDelimiter) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(Object value : coll) {
			if(value != null) {
				if(i > 0) {
					sb.append(sDelimiter);
				}
				sb.append(value.toString());
			}
		}
		return sb.toString();
	}
	
	public static void writeToFile(File f, String s) throws IOException {
		try(PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
			out.print(s);
		}
	}

	public static int count(String s, char charToCount) {
		return s.chars().filter( c -> c == charToCount ).sum();
	}
	
	public static String toString(String s) {
		return s == null ? "" : s;
	}
	
	public static boolean equals(String s1, String s2, boolean bTrueIfBothNull) {
		if(s1 == s2)
			return s1 == null ? bTrueIfBothNull : true;
		else
			return s1 == null ? false : s1.equals(s2);
	}

	public static boolean equals(String s1, String s2) {
		return equals(s1, s2, true);
	}
}

