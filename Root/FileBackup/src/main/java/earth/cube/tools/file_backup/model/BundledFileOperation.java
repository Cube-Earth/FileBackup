package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileRelocator;
import earth.cube.tools.file_backup.commons.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class BundledFileOperation {
		
	private FileName _name;
	private List<FileName> _files = new ArrayList<>();
	private int _nSuffix = 0;
	private BaseFileOperationType _op;
	private RecordFile _file;
	
	@Setter
	private FileRelocator _relocator;
	

	@Accessors(fluent = false)
	public class FileName {

		@Getter @Setter
		protected File _directory;
		
		@Getter @Setter
		protected String _sPrefix;
		
		@Getter @Setter
		protected String _sName;
		
		@Getter @Setter
		protected String _sExtension;
		
		@Getter @Setter
		protected int _nSuffix = -1;

		@Getter @Setter
		private RecordFile _recordFile;
		
		private File _file;

		
		public FileName(RecordFile rfile) {
			_recordFile = rfile;
			File file = _relocator == null ? rfile.getTargetFile(_op) : _relocator.relocate(rfile.getFile());
			_directory = file.getParentFile();
			_sExtension = FileUtil.getRawExtension(file);
			String sName = FileUtil.getNameWithoutExtension(file);
			if(sName.startsWith(FileCollection.XATTR_PREFIX)) {
				_sPrefix = FileCollection.XATTR_PREFIX;
				_sName = sName.substring(FileCollection.XATTR_PREFIX.length());
			}
			else {
				_sName = sName;
			}
		}
		
		public File getFile() {
			if(BundledFileOperation.this._nSuffix != _nSuffix || _files == null) {
				_nSuffix = BundledFileOperation.this._nSuffix;
				_file = new File(_directory, (_sPrefix == null ? "" : _sPrefix) + _sName + (_nSuffix <= 0 ? "" : " (" + _nSuffix + ")") + (_sExtension == null ? "" : _sExtension));
			}
			
			return _file;
		}
		
		public boolean exists() {
			return getFile().exists();
		}
	}
	
	public BundledFileOperation(RecordFile file) throws IOException, SQLException {
		_file = file;
	}
	
	protected void addSidecars(BaseFileOperationType op) throws IOException, SQLException {
		_op = op;
		FileCollection.create().file(_file).addFile().addXattr().addPreciseXmp().all()
		.execute( f -> {
			add(f);
		});
	}
	
	protected boolean isFree() {
		for(FileName name : _files) {
			if(name.exists())
				return false;
		}
		return true;
	}
	
	protected void findFree() {
		while(!isFree()) {
			_nSuffix++;
			if(_nSuffix > 100_000)
				throw new IllegalStateException("Bogus!");
		}
	}

	protected void add(RecordFile file) {
		FileName name = new FileName(file);
		
		// prevent major damages. Verify that all file names are the same.
		if(_name == null)
			_name = name;
		else
			if(!_name.getName().equalsIgnoreCase(name.getName()))
				throw new IllegalStateException("Bogus!");

		_files.add(new FileName(file));
	}
	

	public void delete() throws IOException, SQLException {
		addSidecars(BaseFileOperationType.DELETE);
		findFree();
		for(FileName name : _files) {
			name.getRecordFile().delete(name.getFile());
		}
	}

	// TODO delete
	public void delete(FileRelocator relocator) throws IOException, SQLException {
		addSidecars(BaseFileOperationType.DELETE);
		for(FileName name : _files) {
			name.setDirectory(relocator.relocate(name.getDirectory()));
			name.getRecordFile().delete(name.getFile());
		}
	}

	public void archive() throws IOException, SQLException {
		_op = BaseFileOperationType.ARCHIVE;
		add(_file);
		findFree();
		for(FileName name : _files) {
			name.getRecordFile().archive(name.getFile());
		}
	}

	public void archive(boolean bDelete) throws IOException, SQLException {
		addSidecars(BaseFileOperationType.ARCHIVE);
		findFree();
		for(FileName name : _files) {
			name.getRecordFile().archive(name.getFile(), bDelete);
		}
	}

	public void forcedDelete() throws IOException, SQLException {
		_op = BaseFileOperationType.DELETE;
		add(_file);
		findFree();
		for(FileName name : _files) {
			name.getRecordFile().delete(name.getFile());
		}
	}
}
