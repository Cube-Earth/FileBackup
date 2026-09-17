package earth.cube.tools.zip_web_browser.flattened_directory;

import java.io.File;
import java.util.Date;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import earth.cube.tools.zip_web_browser.utils.DateUtil;
import earth.cube.tools.zip_web_browser.utils.Escaper;
import earth.cube.tools.zip_web_browser.utils.IRenderer;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class FlatEntry implements IRenderer {

	@Getter @Setter
	protected String _sPath;
	
	@JsonIgnore
	@Getter @Setter
	protected String _sName;

	@Getter @Setter
	protected boolean _bDirectory = false;

	@Getter @Setter
	protected long _nSize = Long.MIN_VALUE;

	@Getter @Setter
	protected Date _dLastModified;
	
	@Getter @Setter
	protected Object _data;

	
	protected FlatEntry() {
	}
	
	public FlatEntry(File path) {
		_sPath = path.getAbsolutePath();
		_sName = path.getName();
	}
	
	public void init(Consumer<FlatEntry> func) {
		if(func != null && _nSize == Long.MIN_VALUE) {
			func.accept(this);
			if(_nSize == Long.MIN_VALUE) {
				_nSize = -1;
			}
		}
	}
	
	@Override
	public String getHtml() {
		if(_bDirectory)
			return String.format("<tr><td><a href=\"%s\">%s</a><td></td><td>%s</td></tr>", Escaper.escapeHtml(_sName), Escaper.escapeHtml(_sName), DateUtil.format(_dLastModified));
		else
			return String.format("<tr><td><a href=\"%s\">%s</a><td>%s</td><td>%s</td></tr>", Escaper.escapeHtml(_sName), Escaper.escapeHtml(_sName), _nSize, DateUtil.format(_dLastModified));
	}
	
}