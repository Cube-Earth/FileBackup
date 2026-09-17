package earth.cube.tools.zip_web_browser.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ResponseBuilder {
	
	protected final static Map<String, String> MIME_TYPES;
	
	static {
		Map<String, String> inlineFormats = new HashMap<>();
		inlineFormats.put("txt", "text/plain");
		inlineFormats.put("htm", "text/html");
		inlineFormats.put("html", "text/html");
		inlineFormats.put("pdf", "application/pdf");
		MIME_TYPES = inlineFormats;
	}	
	
	
	
	private HttpServletResponse _resp;
	
	@Setter
	private String _sContentType;
	
	@Setter
	private String _sCharset;

	@Setter
	private int _nLength;
	
	private InputStream _body;
	private boolean _bCloseBodyStream;


	public ResponseBuilder(HttpServletResponse response) {
		_resp = response;
	}
	
	public static ResponseBuilder from(HttpServletResponse response) {
		return new ResponseBuilder(response);
	}
	
	public ResponseBuilder html() {
		_sContentType = "text/html";
		_sCharset = "utf-8";
		return this;
	}

	public ResponseBuilder html(String s) {
		html();
		body(s);
		return this;
	}
	
	public ResponseBuilder clearBody() {
		if(_body != null) {
			if(_bCloseBodyStream)
				try {
					_body.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			_body = null;
		}
		return this;
	}

	public ResponseBuilder body(String s) {
		clearBody();
		byte[] baContent;
		try {
			baContent = s.getBytes(_sCharset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		_nLength = baContent.length;
		_bCloseBodyStream = false;
		_body = new ByteArrayInputStream(baContent);
		return this;
	}

	public ResponseBuilder body(InputStream is, boolean bClose) {
		clearBody();
		_body = is;
		_bCloseBodyStream = bClose;
		return this;
	}
		
	
	public ResponseBuilder contentTypeByPath(String sPath) {
		String sContentType = MIME_TYPES.get(FileUtil.getFileExtension(sPath));
		if(sContentType == null)
			sContentType = "application/octet-stream";
		_sContentType = sContentType;
		return this;
	}
	
	public void send() throws IOException {
		if(_sContentType != null)
			_resp.setContentType(_sContentType);
		
		if(_sCharset != null)
			_resp.setCharacterEncoding(_sCharset);
		
		if(_nLength != -1)
			_resp.setContentLength(_nLength);
		
		StreamUtil.spool(_body, false, _resp.getOutputStream(), false);
		clearBody();
	}
	
	
	public void sendError(Throwable t) throws IOException {
		int nRC = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		if(t instanceof FileNotFoundException) {
			nRC = HttpServletResponse.SC_NOT_FOUND;
		}
		
		_resp.sendError(nRC, t.toString());
	}

}
