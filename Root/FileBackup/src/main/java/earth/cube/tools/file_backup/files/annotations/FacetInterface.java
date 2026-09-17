package earth.cube.tools.file_backup.files.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

import earth.cube.tools.file_backup.files.IFacet;
import earth.cube.tools.file_backup.files.facets.impl.NoFacet;

@Target(TYPE)
public @interface FacetInterface {
	
	Class<? extends IFacet> provides() default NoFacet.class;
	
	boolean optional() default false;
	
	Class<? extends IFacet>[] dependsOn() default {};

}
