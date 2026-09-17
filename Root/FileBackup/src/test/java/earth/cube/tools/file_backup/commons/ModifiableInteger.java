package earth.cube.tools.file_backup.commons;

import lombok.Getter;
import lombok.Setter;

public class ModifiableInteger {
	
	@Getter @Setter
	private int _nValue;

	public ModifiableInteger() {
	}
	
	public ModifiableInteger(int nValue) {
		_nValue = nValue;
	}
	
	public void increment() {
		_nValue++;
	}
	

}
