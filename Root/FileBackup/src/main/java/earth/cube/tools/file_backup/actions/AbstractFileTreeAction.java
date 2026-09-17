package earth.cube.tools.file_backup.actions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.SQLException;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.commons.TimeSpanDueException;

public abstract class AbstractFileTreeAction extends AbstractAction {
	
	protected File _resumeDir;
	
	protected boolean _bDeleteEmptyDirs;
	

	protected boolean acceptDirectory(File file) {
		return true;
	}

	
	protected boolean acceptFile(File file) {
		return true;
	}
	
	
	protected abstract void process(File file) throws IOException, SQLException;

	
	protected void iterate(File dir) throws TimeSpanDueException, IOException, SQLException {
		if(_resumeDir == null)
			_parameters.getSourceScope().setProperty(this, "resumeDir", dir.getAbsolutePath());
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return !Globals.shouldSkip(file) && (file.isDirectory() && acceptDirectory(file)) || (file.isFile() && acceptFile(file));
			}

		});
		
		for(File file : files) {
			if(file.isDirectory()) {
				iterate(file);
			}
			else
				if(_resumeDir != null && dir.compareTo(_resumeDir) < 0)
					_resumeDir = null;
				if(_resumeDir == null) {
					if(file.isFile()) {
						process(file);
						_parameters.getTimeSpan().checkIfDue();
					}
				}
				else {
					_log.debug("iterate: file '" + file.getAbsolutePath() + "' skipped");
				}
		}
		
		if(_bDeleteEmptyDirs && dir.list().length == 0) {
			if(_parameters.isProbing())
				_log.debug("iterate: deleting empty directory '" + dir.getAbsolutePath() + "' -> probing only");
			else {
				_log.debug("iterate: deleting empty directory '" + dir.getAbsolutePath() + "' ...");
				if(!dir.delete())
					throw new IllegalStateException("Could not delete directory '" + dir.getAbsolutePath() + "'!");
			}
		}
	}

	protected void startIterate(File dir) throws TimeSpanDueException, IOException, SQLException {
		if(dir.isFile())
			process(dir);
		else {
			iterate(dir);
			if(_resumeDir != null) { // the resumeDir has been deleted in-between
				_resumeDir = null;
				iterate(dir);
			}
		}
	}
	
	
	public void execute() throws TimeSpanDueException, IOException, SQLException {
		_resumeDir = _parameters.getSourceScope().getPropertyAsFile(this, "resumeDir");
		_log.debug("execute: resume dir = " + _resumeDir);
		_parameters.getSourceScope().resetCache();
		if(_parameters.getDestinationScope() != null)
			_parameters.getDestinationScope().resetCache();
		_parameters.getTimeSpan().start();
		startIterate(_parameters.getSourceDirectory());
		_parameters.getTimeSpan().stop();
		_parameters.getSourceScope().removeProperty(this, "resumeDir");
	}

}
