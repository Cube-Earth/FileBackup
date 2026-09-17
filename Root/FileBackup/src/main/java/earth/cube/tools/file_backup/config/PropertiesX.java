package earth.cube.tools.file_backup.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import lombok.Getter;

public class PropertiesX extends Properties {
	
	private static final long serialVersionUID = 1L;
	
	
	@Getter
	private File _file;

	
	public PropertiesX(File file) throws IOException {
		_file = file;
		try(Reader in = new InputStreamReader(new FileInputStream(file), "utf-8")) {
			load(in);
		}
	}
	
	public void save() throws IOException {
		try(Writer out = new OutputStreamWriter(new FileOutputStream(_file), "utf-8")) {
			store(out, null);
		}
	}

}
