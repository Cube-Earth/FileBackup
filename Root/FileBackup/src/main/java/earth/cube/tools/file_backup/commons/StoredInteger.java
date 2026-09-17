package earth.cube.tools.file_backup.commons;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class StoredInteger {
	
	private int _n;
	
	public int get() {
		return _n;
	}
	
	public void set(int n) {
		_n = n;
	}

	public int increment() {
		return _n++;
	}
	
	public void max(int n) {
		if(n > _n) {
			_n = n;
		}
	}

}
