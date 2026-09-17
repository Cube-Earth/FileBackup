package earth.cube.tools.file_backup.file_filter;

import lombok.Getter;
import lombok.Setter;

public class Command {
	
	@Getter @Setter
	private FilePatternDirective _directive;
	
	@Getter @Setter
	private String _sParameter;

	public static Command from(String sLine) {
		Command cmd = new Command();
		if(!sLine.startsWith("%"))
			throw new IllegalStateException("'%' expected!");
		String[] saArgs = sLine.substring(1).split(" ", 2);
		FilePatternDirective directive = Enum.valueOf(FilePatternDirective.class, saArgs[0].toUpperCase());
		if(directive == null) {
			throw new IllegalArgumentException("Unknow directive '" + saArgs[0] + "'!");
		}
		cmd.setDirective(directive);
		if(!directive.isParameterRequired() && saArgs.length > 1) {
			throw new IllegalArgumentException("Unexpected parameters!");
		}
		if(directive.isParameterRequired() && saArgs.length < 2) {
			throw new IllegalArgumentException("Missing parameters!");
		}
		
		if(directive.isParameterRequired())
			cmd.setParameter(saArgs[1]);
		
		return cmd;
	}
	
}
