package earth.cube.tools.file_backup.files.facets.impl;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.files.IFacetedFile;
import earth.cube.tools.file_backup.files.IFilterFacet;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;

@FacetInterface(dependsOn = { ILinuxFile.class })
public class ProtectedFiles implements IFilterFacet {
	
	private IFacetedFile _host;

	
	public ProtectedFiles(IFacetedFile file) {
		_host = file;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isFilteredOut() {
		return Globals.isProtected(_host.getFacet(ILinuxFile.class).getFile());
	}

}
