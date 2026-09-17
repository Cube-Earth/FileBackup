package earth.cube.tools.file_backup.files;

import java.io.IOException;

public interface IFacetLifecycle extends IFacet {
	
	void onPreInit() throws IOException;

	void onInit() throws IOException;
}
