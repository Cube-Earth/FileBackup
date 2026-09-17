package earth.cube.tools.zip_web_browser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/*
 SSL:
 
// I placed this block right below my class declaration so it runs
// as soon as the class is defined. (this is for localhost testing ONLY!!!!)    
static {
    //for localhost testing only
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
    new javax.net.ssl.HostnameVerifier(){

        public boolean verify(String hostname,
                javax.net.ssl.SSLSession sslSession) {
            if (hostname.equals("localhost")) {
                return true;
            }
            return false;
        }
    });
}

// then in an init function, I set it all up here
this.secureAppServer = new NanoHTTPD(9043);
File f =new File("src/main/resources/key001.jks");
System.setProperty("javax.net.ssl.trustStore", f.getAbsolutePath());
this.secureAppServer.setServerSocketFactory(new SecureServerSocketFactory(NanoHTTPD.makeSSLSocketFactory("/" +f.getName(), "myawesomepassword".toCharArray()), null));

this.secureAppServer.start(); 
 */

// https://www.baeldung.com/nanohttpd (also SSL)


public class Application extends RouterNanoHTTPD {
	
	public final static Properties CONFIG;

	static {
		Properties props = new Properties();
		try (Reader reader = new InputStreamReader(Application.class.getResourceAsStream("application.properties"), "utf-8")) {
			props.load(reader);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		CONFIG = props;
	}
	
	
    public Application() throws IOException {
        super(Integer.parseInt(CONFIG.getProperty("server.port")));
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
    
    @Override
    public void addMappings() {
    	super.addMappings();
    	addRoute("/", IndexHandler.class); 
    	addRoute("/ZipBrowser/.*", ZipBrowserHandler.class); 
    }

    /*
    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.GET) {
            String itemIdRequestParameter = session.getParameters().get("itemId").get(0);
            return newFixedLengthResponse("Requested itemId = " + itemIdRequestParameter);
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, 
            "The requested resource does not exist");
    }
    */

    public static void main(String[] args ) throws IOException {
        new Application();
    }



}