package earth.cube.tools.zip_web_browser.utils;

public class Escaper {

	public static String escapeHtml(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
	
	public static String escapeHtmlString(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
}
