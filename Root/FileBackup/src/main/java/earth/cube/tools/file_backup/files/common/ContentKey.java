package earth.cube.tools.file_backup.files.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import earth.cube.tools.file_backup.commons.StringUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentKey implements IContentKey {
	
	@Getter
	protected String _sSha256;
	
	@Getter
	protected long _nSize;
	
	@Getter
	protected String _sExtension;
	
	public ContentKey(IContentKey cnt) {
		_sSha256 = cnt.getSha256();
		_nSize = cnt.getSize();
		_sExtension = cnt.getExtension();
	}
	
	@Override
	public int hashCode() {
		return (StringUtil.toString(_sSha256) + '|' + _nSize + '|' + StringUtil.toString(_sExtension)).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return equals(this, o);
	}	

	public static boolean equals(Object o1, Object o2) {
		if(!(o1 instanceof IContentKey && o2 instanceof IContentKey))
			return false;
		
		IContentKey k1 = (IContentKey) o1;
		IContentKey k2 = (IContentKey) o2;
		
		return StringUtil.equals(k1.getSha256(), k2.getSha256(), false) && k1.getSize() == k2.getSize() && StringUtil.equals(k1.getExtension(), k2.getExtension());
	}	
}
