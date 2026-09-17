package earth.cube.tools.file_backup.commons;

import java.util.HashSet;
import java.util.Set;

public class DebugScope {
	
	protected final static DebugScope INSTANCE = new DebugScope();
	
	protected Set<String> _markers = new HashSet<>();
	
	
	public static void reset() {
		INSTANCE._markers.clear();
	}
	
	public static void set(String sMarker) {
		INSTANCE._markers.add(sMarker);
	}

	public static void clear(String sMarker) {
		INSTANCE._markers.remove(sMarker);
	}
	
	public static boolean has(String sMarker) {
		return INSTANCE._markers.contains(sMarker);
	}
	

}
