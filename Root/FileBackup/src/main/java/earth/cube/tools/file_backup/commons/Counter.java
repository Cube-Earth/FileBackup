package earth.cube.tools.file_backup.commons;

import lombok.Getter;
import lombok.Setter;

public class Counter {
	
	private int _n;
	
	@Getter @Setter
	private Counter _parent;
	
	public Counter() {
	}

	public Counter(Counter parent) {
		_parent = parent;
	}

	public Counter(int n) {
		_n = n;
	}
	
	public int increment() {
		return _n++;
	}

	public int get() {
		return _n + (_parent != null ? _parent.get() : 0);
	}
}
