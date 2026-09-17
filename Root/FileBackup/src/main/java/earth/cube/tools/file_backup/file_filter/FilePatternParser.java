package earth.cube.tools.file_backup.file_filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FilePatternParser {
	
	protected File _baseDir;
	protected IFilePattern _parent;
	private FilePatternList _container;
	private FileStringPatterns _pattern;
	private Command _cmd;
	
	public FilePatternParser(File baseDir, FilePatternList parent) {
		_baseDir = baseDir;
		_parent = parent;
		_container = new FilePatternList(baseDir, parent);
	}
	
	
	protected boolean parseSection(BufferedReader reader) throws IOException {
		_pattern = new FileStringPatterns(_baseDir);
		String sLine = reader.readLine();
		while(sLine != null) {
			if(sLine.trim().length() == 0 || sLine.startsWith("#"))
				continue;
			if(sLine.startsWith("%")) {
				handleDirective(false);
				_cmd = Command.from(sLine);
				return true;
			}
			_pattern.add(sLine);
			sLine = reader.readLine();
		}
		handleDirective(false);
		return false;
	}

	protected void handleDirective(boolean bStart) {
		if(_cmd == null) {
			_container.add(_pattern);
			return;
		}
		
		if(bStart ^ !_cmd.getDirective().isBlockDirective()) {
			return;
		}
		
		switch(_cmd.getDirective()) {
			case INHERIT:
				_container.add(_parent);
				_cmd = null;
				break;

			case TEMPLATE:
				_container.addTemplate(_cmd.getParameter(), _pattern);
				break;

			case USE:
				_container.add(_container.getTemplate(_cmd.getParameter(), true));
				break;

			default:
				throw new IllegalArgumentException("Unexpected directive '" + _cmd.getDirective() + "'!");
		}
	}
	
	
	public void parse(BufferedReader reader) throws IOException {
		while(parseSection(reader)) {
			handleDirective(true);
		}
	}



}
