package earth.cube.tools.file_backup.files.facets.api;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.IValidator;
import earth.cube.tools.file_backup.files.IFacet;

public interface IGenericFile extends IValidator, IFacet {

	File getFile();

	String getName();

	String getExtension();

	boolean isValid();

	String getSha256(Date treshold) throws IOException;

}