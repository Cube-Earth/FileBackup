package earth.cube.tools.zip_web_browser;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class ZipBrowserHandler extends GeneralHandler {
	

	
	
    @Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        return NanoHTTPD.newFixedLengthResponse("Retrieving store for id = "
          + session.getUri() + ","  + urlParams.get("storeId"));
    }
}