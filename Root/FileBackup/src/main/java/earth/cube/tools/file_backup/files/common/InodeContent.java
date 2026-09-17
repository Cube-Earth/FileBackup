package earth.cube.tools.file_backup.files.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InodeContent extends InodeKey implements IInodeContent {
	
	@Getter
	protected String _sSha256;
	
	@Getter
	protected long _nSize;
	
	@Getter
	protected String _sExtension;
	
	public InodeContent(IInodeKey inode, IContentKey cnt) {
		super(inode);
		init(cnt);
	}
	
	
	public InodeContent(IInodeContent cnt) {
		super(cnt);
		init(cnt);
	}
	
	protected void init(IContentKey cnt) {
		_sSha256 = cnt.getSha256();
		if(_sSha256 == null || _sSha256.length() == 0)
			throw new IllegalStateException("SHA256 is mandatory!");
		_nSize = cnt.getSize();
		_sExtension = cnt.getExtension();
	}
	
}
