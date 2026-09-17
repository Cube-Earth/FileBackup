package earth.cube.tools.file_backup.commons;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RudeIniParser implements Closeable {
	
	private BufferedReader _in;
	private boolean _bEof;

	public RudeIniParser(File file, String sSectionName) throws IOException {
		_in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		sSectionName = sSectionName.toLowerCase();
		
		String sLine = _in.readLine();
		while(sLine != null && !sSectionName.equals(getSectionName(sLine))) {
			sLine = _in.readLine();
		}
		if(sLine == null)
			_bEof = true;
	}
	
	
	protected boolean isSection(String sLine) {
		sLine = sLine.trim();
		return sLine.startsWith("[") && sLine.endsWith("]");
	}
	
	protected String getSectionName(String sLine) {
		sLine = sLine.trim();
		return isSection(sLine) ? sLine.substring(1, sLine.length() - 2).toLowerCase() : null;
	}
	
	public String readLine() throws IOException {
		if(_bEof)
			return null;
		String sLine = _in.readLine();
		if(isSection(sLine)) {
			_bEof = true;
			return null;
		}
		return sLine;
	}

	@Override
	public void close() throws IOException {
		_in.close();
	}

}
