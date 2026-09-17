package earth.cube.tools.file_backup.files;

import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.commons.IFuncFunction;

public interface IFacetedFile {

	
	boolean addFacet(IFacet facet) throws IOException, SQLException;
	
	boolean addFacets(Class<? extends IFacet>... facetClasses) throws IOException, SQLException;

	<T extends IFacet> boolean hasFacet(Class<T> clazz);
	
	<T extends IFacet> T getFacet(Class<T> clazz);
	
	<T extends IFacet, U> U withFacet(Class<T> clazz, IFuncFunction<T, U> func, U defaultValue) throws IOException, SQLException;
	
	
	boolean isValid();
	
	FilePath getPath();
	
}
