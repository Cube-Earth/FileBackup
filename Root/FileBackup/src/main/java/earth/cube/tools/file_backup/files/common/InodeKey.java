package earth.cube.tools.file_backup.files.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InodeKey implements IInodeKey {
	
	@Getter
	protected long _nInodeId;
	
	@Getter
	protected Date _dInodeChangedTime;
	

	public InodeKey(IInodeKey inode) {
		_nInodeId = inode.getInodeId();
		_dInodeChangedTime = inode.getInodeChangedTime();
	}
	
	@Override
	public int hashCode() {
		return (_nInodeId + "|" + _dInodeChangedTime.getTime()).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return equals(this, o);
	}
	

	public static boolean equals(Object o1, Object o2) {
		if(!(o1 instanceof IInodeKey && o2 instanceof IInodeKey))
			return false;
		
		IInodeKey k1 = (IInodeKey) o1;
		IInodeKey k2 = (IInodeKey) o2;
		
		return k1.getInodeId() == k2.getInodeId() && k1.getInodeChangedTime() == k2.getInodeChangedTime();
	}
	
	public static String toString(IInodeKey key) {
		return key.getInodeId() + '|' + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(key.getInodeChangedTime());
	}
}
