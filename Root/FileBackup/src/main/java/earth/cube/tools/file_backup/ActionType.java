package earth.cube.tools.file_backup;

import earth.cube.tools.file_backup.actions.BanFilesAction;
import earth.cube.tools.file_backup.actions.CleanDatabaseAction;
import earth.cube.tools.file_backup.actions.CopyFilesToNas;
import earth.cube.tools.file_backup.actions.DeduplicateAllFilesAction;
import earth.cube.tools.file_backup.actions.DeduplicateMostFilesAction;
import earth.cube.tools.file_backup.actions.FindClonedDirectoriesAction;
import earth.cube.tools.file_backup.actions.IAction;
import earth.cube.tools.file_backup.actions.MoveFilesToNas;
import earth.cube.tools.file_backup.actions.RegisterFilesInDatabaseAction;
import earth.cube.tools.file_backup.actions.RemoveAllOtherDuplicatesAction;
import earth.cube.tools.file_backup.actions.RemoveOtherDuplicatesAction;
import earth.cube.tools.file_backup.actions.RemoveTheseDuplicatesAction;
import earth.cube.tools.file_backup.actions.ShowDuplicatesAction;
import lombok.Getter;

public enum ActionType {
	DEDUPLICATE_ALL(true, DeduplicateAllFilesAction.class),
	DEDUPLICATE_MOST(true, DeduplicateMostFilesAction.class),
	REGISTER_FILES(true, RegisterFilesInDatabaseAction.class),
	BAN_FILES(true, BanFilesAction.class),
	FIND_CLONED_DIRECTORIES(true, FindClonedDirectoriesAction.class),
	REMOVE_THESE_DUPLICATES(true, RemoveTheseDuplicatesAction.class),
	REMOVE_OTHER_DUPLICATES(true, RemoveOtherDuplicatesAction.class),
	REMOVE_ALL_OTHER_DUPLICATES(true, RemoveAllOtherDuplicatesAction.class),
	SHOW_DUPLICATES(true, ShowDuplicatesAction.class),
	COPY_FILES(false, CopyFilesToNas.class),
	MOVE_FILES(false, MoveFilesToNas.class),
	CLEAN_DATABASE(true, CleanDatabaseAction.class),
	
	// shortcuts
	DDA(DEDUPLICATE_ALL),
	DDM(DEDUPLICATE_MOST),
	RF(REGISTER_FILES),
	BF(BAN_FILES),
	FCD(FIND_CLONED_DIRECTORIES),
	RTD(REMOVE_THESE_DUPLICATES),
	ROD(REMOVE_OTHER_DUPLICATES),
	RAOD(REMOVE_ALL_OTHER_DUPLICATES),
	SD(SHOW_DUPLICATES),
	CF(COPY_FILES),
	MF(MOVE_FILES),
	CB(CLEAN_DATABASE)
	;

	@Getter
	private ActionType _origin;
	
	@Getter
	private boolean _bNoDestinationDirecory;
	
	@Getter
	private Class<? extends IAction> _clazz;
	
	
	private ActionType(boolean bNoDestinationDirectory, Class<? extends IAction> clazz) {
		_bNoDestinationDirecory = bNoDestinationDirectory;
		_clazz = clazz;
	}
	
	private ActionType(ActionType origin) {
		_origin = origin;
	}
	
	public static ActionType from(String s) {
		if(s == null)
			return null;
		ActionType type = Enum.valueOf(ActionType.class, s.toUpperCase());
		if(type.getOrigin() != null)
			type = type.getOrigin();
		return type;
	}
	
}
