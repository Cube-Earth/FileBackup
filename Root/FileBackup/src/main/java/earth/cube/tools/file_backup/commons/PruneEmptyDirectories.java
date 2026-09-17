package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import earth.cube.tools.file_backup.Scope;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PruneEmptyDirectories {
	
	
	protected Set<File> _dirs = new HashSet<>();
	
	@Setter
	protected Scope _scope;
	
	public PruneEmptyDirectories(Scope scope) {
		_scope = scope;
	}
	
	public void add(File dir) {
		_dirs.add(dir);
	}
	
	protected void delete(File dir) {
		if(dir.isDirectory()) {
			try {
				FileCollection.create().scope(_scope).addOrphanedSidecars(dir).execute( f -> {
					f.bundle().forcedDelete();
				});
			} catch (IOException | SQLException e) {
				throw new RuntimeException(e);
			}
			
			if(dir.list().length == 0) {
				log.debug("delete: deleting empty directory '" + dir.getAbsolutePath() + "' ...");
				if(!dir.delete()) {
					throw new IllegalStateException("Could not delete directory '" + dir.getAbsolutePath() + "'!");
				}
				delete(dir.getParentFile());
			}
		}
	}

	public void cleanUp() {
		List<File> dirs = new ArrayList<>(_dirs);
		_dirs.clear();
		dirs.sort(new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				return -f1.compareTo(f2);
			}
			
		});
		
		for(File dir : dirs) {
			delete(dir);
		}
		
	}

}
