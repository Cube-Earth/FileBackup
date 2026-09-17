package earth.cube.tools.file_backup.actions;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractAction implements IAction {

	protected Logger _log = LogManager.getLogger(getClass());
	
	@Getter @Setter
	protected ActionParameters _parameters;

	public AbstractAction() {
		super();
	}	
}