package earth.cube.tools.file_backup.commons;

import java.io.Closeable;
import java.io.IOException;

public class Dirty implements Closeable {
	
	private boolean _bDirty;
	private Dirty _parent;

	
	public Dirty() {
	}
	
	public Dirty(Dirty dirty) {
		_bDirty = false;
		_parent = dirty;
	}
	
	public void set(boolean bDirty) {
		_bDirty = bDirty;
		if(_parent != null)
			_parent.set(bDirty);
	}

	public boolean is() {
		return _bDirty;
	}
	
	public <T> T get(T oldValue, T newValue) {
		_bDirty = oldValue != newValue && (newValue == null || !newValue.equals(oldValue));
		if(_bDirty && _parent != null)
			_parent.set(true);
		return newValue;
	}
	
	@Override
	public void close() throws IOException {
	}

}
