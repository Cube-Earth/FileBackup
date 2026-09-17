package earth.cube.tools.file_backup.actions;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.TimeSpan;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionParameters {

	@Getter @Setter
	protected TimeSpan _timeSpan = new TimeSpan();

	@Getter
	protected Scope _sourceScope;
	
	@Getter
	protected Scope _destinationScope;

	@Getter
	protected boolean _bProbing;
	
	@Getter @Setter
	protected String _sRemark;
	
	@Getter
	protected File _sourceDirectory;
	
	@Getter
	protected File _destinationDirectory;
	
	@Getter @Setter
	protected PrintStream _out;
	
	
	public void setSourceScope(Scope scope) {
		scope.setProbing(_bProbing);
		_sourceScope = scope;		
	}
	
	public void setDestinationScope(Scope scope) {
		scope.setProbing(_bProbing);
		_destinationScope = scope;		
	}
	
	public void setProbing(boolean bProbing) {
		_bProbing = bProbing;
		if(_sourceScope != null) {
			_sourceScope.setProbing(bProbing);
		}
		if(_destinationScope != null) {
			_destinationScope.setProbing(bProbing);
		}
	}
	
	public void setSourceDirectory(File dir) throws IOException, SQLException {
		_sourceDirectory = dir;
		if(_sourceScope == null) {
			File srcRootDir = Scope.findRootDirectory(dir);
			setSourceScope(_destinationScope != null && _destinationScope.getRootDirectory().equals(srcRootDir) ? _destinationScope : new Scope(srcRootDir == null ? dir : srcRootDir));
		}
	}

	public void setDestinationDirectory(File dir) throws IOException, SQLException {
		_destinationDirectory = dir;
		if(_destinationDirectory == null) {
			File dstRootDir = Scope.findRootDirectory(dir);
			setDestinationScope(_sourceScope != null && _sourceScope.getRootDirectory().equals(dstRootDir) ? _sourceScope : new Scope(dstRootDir == null ? dir : dstRootDir));
		}
	}
	
	public void checkNoDestinationDirectory() {
		if(_destinationDirectory != null)
			throw new IllegalStateException("Destination directory is unexpected!");
	}
	
	public void cleanUp() {
		_sourceScope.cleanUp();
		if(_destinationScope != null && _destinationScope != _sourceScope) {
			_destinationScope.cleanUp();
		}
	}

	public void close() {
		IOException excp = null;
		try {
			if(_sourceScope != null) {
				_sourceScope.saveProperties();
				_sourceScope.getStatistics().save();
				_sourceScope.close();
			}
		}
		catch(IOException e) {
			log.error("close: Exception occurred -->", e);
			excp = e;
		}

		try {
			if(_destinationScope != null && _destinationScope != _sourceScope) {
				_destinationScope.saveProperties();
				_destinationScope.getStatistics().save();
				_destinationScope.close();
			}
		}
		catch(IOException e) {
			log.error("close: Exception occurred -->", e);
			excp = excp == null ? e : excp;
		}
		
		if(excp != null)
			throw new RuntimeException(excp);
	}

}