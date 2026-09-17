package earth.cube.tools.file_backup.files.facets.impl;

import java.io.IOException;

import earth.cube.tools.file_backup.files.IFacetedFile;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.IFacetLifecycle;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;

@FacetInterface(dependsOn = { ILinuxFile.class })
public class Sha256Calculator implements IFacetLifecycle {
	
	protected IFacetedFile _host;
	
	public Sha256Calculator(IFacetedFile file) {
		_host = file;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void onPreInit() throws IOException {
		ILinuxFile file = _host.getFacet(ILinuxFile.class);
		String sSha256 = file.getSha256();
		if(sSha256 == null || sSha256.length() == 0)
			file.calculateSha256();
	}


	@Override
	public void onInit() throws IOException {
	}


}
