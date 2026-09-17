package earth.cube.tools.file_backup.file_filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FileMatched {
	
	public final static FileMatched NOT_MATCHED = new FileMatched(false, false);
	
	public final static FileMatched MATCHED = new FileMatched(true, false);

	public final static FileMatched NOT_MATCHED_STOP = new FileMatched(false, false);
	
	public final static FileMatched MATCHED_STOP = new FileMatched(true, false);
	
	@Getter
	private boolean _bMatched;
	
	@Getter
	private boolean _bStopEvaluation;
	

}
