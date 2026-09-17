package earth.cube.tools.file_backup.file_filter;

import lombok.Getter;
import lombok.Setter;

public enum FilePatternDirective {
	
	INHERIT(false ,false),
	TEMPLATE(true, true),
	USE(true, false);
	
	@Getter
	private boolean _bBlockDirective;
	
	@Getter @Setter
	private boolean _bParameterRequired;

	FilePatternDirective(boolean bParameterRequired, boolean bBlockDirective) {
		_bParameterRequired = bParameterRequired;
		_bBlockDirective = bBlockDirective;
	}

}
