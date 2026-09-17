package earth.cube.tools.file_backup.files;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.commons.IFuncFunction;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.facets.impl.BannedFile;
import earth.cube.tools.file_backup.files.facets.impl.GenericFile;
import earth.cube.tools.file_backup.files.facets.impl.LifecycleBoundary;
import earth.cube.tools.file_backup.files.facets.impl.LinuxFile;
import earth.cube.tools.file_backup.files.facets.impl.NoFacet;
import earth.cube.tools.file_backup.files.facets.impl.ProtectedFiles;
import earth.cube.tools.file_backup.files.facets.impl.Sha256Calculator;
import lombok.Getter;

public class FacetedFile implements IFacetedFile {
	
	protected Logger _log = LogManager.getLogger();
	
	protected Map<Class<?>,IFacet> _facets = new LinkedHashMap<>();
	
	private boolean _bFilteredOut;
	private boolean _bValid = true;
	private int _nLastBoundaryIdx;
	
	@Getter
	protected FilePath _path;

	private AdvertisedAction _advertisedAction = AdvertisedAction.NONE;
	
	
	public FacetedFile(FilePath path) {
		_path = path;
	}
	
	public boolean addFacet(IFacet facet) throws IOException, SQLException {
		if(facet == null) {
			return false;
		}
		FacetInterface annot = facet.getClass().getAnnotation(FacetInterface.class);
		
		if(annot != null && annot.dependsOn() != null)
			for(Class<? extends IFacet> dependsOnClass : annot.dependsOn()) {
				IFacet dependsOn = _facets.get(dependsOnClass);
				if(dependsOn == null || (dependsOn != null && !dependsOn.isValid())) {
					if(!annot.optional())
						_bValid = false;
					return false;
				}
			}
		
		if(facet instanceof IFacetLifecycle) {
			((IFacetLifecycle) facet).onPreInit();
		}
		
		if(!facet.isValid()) {
			if(annot != null && !annot.optional())
				_bValid = false;
			return false;
		}
		
		if(facet instanceof LifecycleBoundary) {
			List<IFacet> facets = new ArrayList<>(_facets.values());
			int n = _facets.size();
			for(int i = _nLastBoundaryIdx; i <= n; i++) {
				IFacet item = facets.get(i);
				if(item instanceof IFacetLifecycle) {
					((IFacetLifecycle) item).onInit();
				}
			}
			_nLastBoundaryIdx = n;
		}
		
		if(facet instanceof IAdvertisedActionFacet) {
			_advertisedAction = _advertisedAction.merge(((IAdvertisedActionFacet) facet).getAdvertisedAction());
			// TODO: Do something with _advertisedAction
		}
		
		if(facet instanceof IFilterFacet) {
			_bFilteredOut |= ((IFilterFacet) facet).isFilteredOut();
			return true;
		}
			
		if(annot != null && !annot.provides().equals(NoFacet.class)) {
			_facets.put(annot.provides(), facet);
			if(facet instanceof LifecycleBoundary) {
				_nLastBoundaryIdx = _facets.size();  // skip LifecycleBoundary
			}
		}
		return true;
	}
	
	
	protected IFacet instantiateFacet(Class<? extends IFacet> facetClass) {
		IFacet facet = null;
		try {
			Constructor<?> c = facetClass.getClass().getDeclaredConstructor(IFacetedFile.class);
			facet = (IFacet) c.newInstance(this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			_log.error("Exception occurred -->", e);
			throw new RuntimeException(e);
		}
		return facet;
	}

	
	public boolean addFacets(Class<? extends IFacet>... facetClasses) throws IOException, SQLException { //TODO: handle warning??
		boolean bSuccess = true;
		for(Class<? extends IFacet> facetClass : facetClasses) {
			IFacet facet = instantiateFacet(facetClass);
			bSuccess &= addFacet(facet);
		}
		return bSuccess;
	}
	
	public boolean isValid() {
		return _bValid && !_bFilteredOut;
	}
	
	protected void addStandardFacets() throws IOException, SQLException {
		addFacets(GenericFile.class, LinuxFile.class, ProtectedFiles.class, Sha256Calculator.class, BannedFile.class, LifecycleBoundary.class);
	}

	@Override
	public <T extends IFacet> boolean hasFacet(Class<T> clazz) {
		return _facets.containsKey(clazz);
	}
	
	@Override
	public <T extends IFacet> T getFacet(Class<T> clazz) {
		return (T) _facets.get(clazz);
	}
	
	
	@Override
	public <T extends IFacet, U> U withFacet(Class<T> clazz, IFuncFunction<T, U> func, U defaultValue) throws IOException, SQLException {
		T api = (T) _facets.get(clazz);
		return api == null ? defaultValue : func.apply(api);
		
	}



	
}
