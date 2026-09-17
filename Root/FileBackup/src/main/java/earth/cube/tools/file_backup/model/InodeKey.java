package earth.cube.tools.file_backup.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InodeKey {
	
	@Getter
	private String _sSha256;
	
	@Getter
	private String _sExension;
	
	@Getter
	private long _nSize;
	
	@Override
	public String toString() {
		return _sSha256 + ',' + _sExension + ',' + _nSize;
	}

}
