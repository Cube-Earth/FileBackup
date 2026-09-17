package earth.cube.tools.file_backup.files;

import earth.cube.tools.file_backup.commons.ValidationException;

public enum AdvertisedAction {
	
	NONE,
	BAN_FILE,
	BAN_DIRECTORY,
	DEDUPLICATE;
	
	public AdvertisedAction merge(AdvertisedAction action) {
		if(equals(NONE))
			return action;
		if(action == null || action.equals(NONE))
			return this;
		if(equals(BAN_FILE) && action.equals(BAN_DIRECTORY))
			return BAN_DIRECTORY;
		if(equals(BAN_DIRECTORY) && action.equals(BAN_FILE))
			return BAN_DIRECTORY;
		throw new ValidationException("Conflicting action " + this + " and " + action);
	}

}
