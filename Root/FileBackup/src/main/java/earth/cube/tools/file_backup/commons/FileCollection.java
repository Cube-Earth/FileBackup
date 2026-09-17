package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.files.common.IContentKey;
import earth.cube.tools.file_backup.model.LinuxFile;
import earth.cube.tools.file_backup.model.LinuxInode;
import earth.cube.tools.file_backup.model.RecordFile;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Accessors(fluent = true)
public class FileCollection {

	public final static String XATTR_PREFIX = "._";

	protected final static File CONFLICT = new File("\0");
	
	protected static class RecordFileComparator implements Comparator<LinuxFile> {

		@Override
		public int compare(LinuxFile o1, LinuxFile o2) {
			int n = Integer.compare(o1.getLinkCount(), o1.getLinkCount());
			if(n == 0)
				n = -o1.getFile().compareTo(o2.getFile());
			return n;
		}
			
	}
	
	
	protected Map<File, RecordFile> _files = new LinkedHashMap<>();

	protected RecordFile _rfile;
	protected File _file;
	protected boolean _bValid;
	
	@Setter
	protected IContentKey _sha256;
	
	@Setter
	protected Scope _scope;

	protected Map<File, RecordFile> _recalc = new LinkedHashMap<>();

	protected RecordFile _master;

	
	@Getter @Setter
	protected FileCollection _bin;
	
	@Getter @Setter
	protected FileCollection _main;

	@Setter
	protected long _nMaximumFileSize = -1;

	
	
	public static FileCollection create() {
		return new FileCollection();
	}
	
	
	public FileCollection file(RecordFile file) throws IOException {
		log.debug("file: file = " + file.getFile().getAbsolutePath());
		
		if(_scope == null)
			_scope = file.getScope();

		if(_main == null)
			_bin = FileCollection.create().main(this).file(file);

		if(file.isValid()) {
			if(isSidecar(file.getFile())) {
				log.debug("file: is sidecar file. ignoring ...");
				_bValid = false;
			}
			else {
				_bValid = true;
				_rfile = file;
				_file = file.getFile();
				_sha256 = file.getInode();
			}
		}
		else {
			log.debug("file: is not valid. ignoring ...");
			_bValid = false;
		}
		return this;
	}
	

	public FileCollection addXattr() throws IOException, SQLException {
		if(!_bValid) {
			return this;
		}
		
		if(!_file.getName().startsWith("._")) {
			File file = new File(_file.getParent(), "._" + _file.getName());
			if(file.exists() && CloseableAction.execute(new XattrFile(file), f -> f.isValid())) {
				log.debug("addXattr: file = " + file.getAbsolutePath());
				_bin.add(new RecordFile(_rfile.getScope(), file));
			}
		}
		return this;
	}
	
	
	public FileCollection addPreciseXmp() throws IOException, SQLException {
		if(!_bValid) {
			return this;
		}

		// XMP metadata files
		File xmp = null;
		File base = null;
		List<File> files = FileUtil.findSiblings(_file);
		for(File file : files) {
			if(FileUtil.getExtension(file).equals(".xmp")) {
				if(xmp == null) {
					xmp = file;
				}
				else {
					xmp = CONFLICT;
				}
			}
			else {
				if(base == null) {
					base = file;
				}
				else {
					base = CONFLICT;
				}
			}
		}
		
		if(xmp != null && xmp != CONFLICT && base != null && base != CONFLICT) {
			log.debug("addPreciseXmp: file = " + xmp.getAbsolutePath());
			_bin.add(new RecordFile(_rfile.getScope(), xmp));
		}
		
		return this;
	}
	
	public FileCollection addOrphanedSidecars(File dir) {
		List<File> files = Arrays.asList(dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
			
		}));
		
		files.stream().filter( f -> {
			try {
				String sName = f.getName();
				if(!sName.startsWith(XATTR_PREFIX))
					return false;
				if(new File(f.getParentFile(), sName.substring(XATTR_PREFIX.length())).exists())
					return false;
				if(!CloseableAction.execute(new XattrFile(f), g -> g.isValid()))
					return false;
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).forEach( f -> {
			add(f);
		});
		
		files.stream().filter( f -> {
			return isXmpSidecar(f);
		}).forEach( f -> {
			String sName = FileUtil.getNameWithoutExtension(f);
			long n = files.stream().filter( g -> FileUtil.getNameWithoutExtension(g.getName()).equalsIgnoreCase(sName) && !FileUtil.getExtension(g).equals(".xmp") ).count();
			if(n == 0) {
				add(f);
			}
		});
		
		
		return this;
	}
	
	public FileCollection addUnpreciseXmp() throws IOException, SQLException {
		if(!_bValid) {
			return this;
		}

		// XMP metadata files (for image files)
		List<File> files = FileUtil.findFilesByName(_file.getParentFile(), FileUtil.getNameWithoutExtension(_file) + ".xmp");
		for(File file : files) {
			log.debug("addUnpreciseXmp: file = " + file.getAbsolutePath());
			_bin.add(new RecordFile(_rfile.getScope(), file));
		}
		
		return this;
	}
	
	public FileCollection addFilesWithSameSha256() throws IOException, SQLException {
		if(_sha256 == null) {
			return this;
		}
		
		_master = null;
		if(_nMaximumFileSize != -1 &&  _file.length() < _nMaximumFileSize) {
			_bin.addFilesWithSameSha256();
			return this;
		}

		String sSha256 = _sha256.getSha256();
		long nSize = _sha256.getSize();
		String sExtension = _sha256.getExtension();
		
		RecordFileComparator comparator = new RecordFileComparator();

		Query.perform(_scope.getConnection(), q -> q.query("select f.* from file f, inode i where f.inode=i.id and f.valid=1 and i.sha256='%s' and i.size=%s and i.extension='%s'", sSha256, nSize, sExtension.replace("'", "''")).execute().cache().process( rs -> {
			RecordFile file = new RecordFile(_scope, rs);
			if(file.isValid() && !isSidecar(file.getFile())) {
				LinuxInode inode = file.getInode();
				if(inode.getSize() == nSize 
						&& inode.getExtension().equals(sExtension) 
						&& inode.getSha256().equals(sSha256))
//						&& inode.getSha256Source() == DataSource.CALCULATED)
					
					if(_sha256.getSha256().equals(file.getCalculatedSha256FromInode())) {	// paranoia mode			
						log.debug("addFilesWithSameSha256: file = " + file.getFile().getAbsolutePath());
						add(file);
					
						if(_master == null || comparator.compare(file, _master) > 0) {
							_master = file;
						}
					}
			}
			log.debug("addFilesWithSameSha256: master = " + (_master == null ? null : _master.getFile().getAbsolutePath()));
		}));
	
		return this;
	}
	
	public void add(RecordFile file) {
		if(!_files.containsKey(file.getFile()))
			_files.put(file.getFile(), file);
	}

	public void add(File file) {
		if(!_files.containsKey(file)) {
			try {
				_files.put(file, new RecordFile(_scope, file));
			} catch (IOException | SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}


	public FileCollection addMasterWithSameSha256() throws IOException, SQLException {
		if(_sha256 == null) {
			return this;
		}
		
		_master = null;
		
		if(_nMaximumFileSize != -1 && _file.length() < _nMaximumFileSize) {
			_bin.addMasterWithSameSha256();
			return this;
		}

		String sSha256 = _sha256.getSha256();
		long nSize = _sha256.getSize();
		String sExtension = _sha256.getExtension();
		
		RecordFileComparator comparator = new RecordFileComparator();

		Query.perform(_scope.getConnection(), q -> q.query("select f.* from file f, inode i where f.inode=i.id and f.valid=1 and i.sha256='%s' and i.size=%s and i.extension='%s'", sSha256, nSize, sExtension.replace("'", "''")).execute().cache().process( rs -> {
			RecordFile file = new RecordFile(_scope, rs);
			if(file.isValid() && !isSidecar(file.getFile())) {
				LinuxInode inode = file.getInode();
				if(inode.getSize() == nSize 
						&& inode.getExtension().equals(sExtension) 
						&& inode.getSha256().equals(sSha256))
//						&& inode.getSha256Source() == DataSource.CALCULATED)
					
					if(_sha256.getSha256().equals(file.getCalculatedSha256FromInode())) {	// paranoia mode			
						log.debug("addMasterWithSameSha256: file = " + file.getFile().getAbsolutePath());
					
						if(_master == null || comparator.compare(file, _master) > 0) {
							_master = file;
						}
					}
			}
		}));
		log.debug("addMasterWithSameSha256: master = " + (_master == null ? null : _master.getFile().getAbsolutePath()));
		
		if(_master != null)
			add(_master);
		else
			if(_main != null)
				add(_rfile);
			else
				_bin.add(_rfile);
	
		return this;
	}
	
	public FileCollection storeMaster(StoredObject<RecordFile> master) {
		master.setObject(_master);
		return this;
	}
	
	public FileCollection addFileIfMasterExists() {
		if(_master != null) {
			add(_rfile);
		}
		return this;
	}

	public FileCollection addFileIfMasterDoesNotExist() {
		if(_bValid && _master == null) {
			add(_rfile);
		}
		return this;
	}

	protected FileCollection removeSameInode(RecordFile rfile) {
		if(_bValid && rfile != null) {
			for(Entry<File, RecordFile> e : new HashMap<>(_files).entrySet()) {
				if(e.getValue().getNodeId() == rfile.getNodeId()) {
					log.debug("removeSameInode: file = " + e.getValue().getFile().getAbsolutePath());
					_files.remove(e.getKey());
				}
			}
		}
		return this;
	}
	
	public FileCollection removeFileInode() {
		return removeSameInode(_rfile);
	}
	
	public FileCollection removeMasterInode() {
		return removeSameInode(_master);
	}
	
	protected FileCollection recalc(RecordFile file) throws IOException, SQLException {
		if(_bValid && file != null) {
			log.debug("recalc: file = " + file.getFile().getAbsolutePath());
			Scope scope = file.getScope();
			scope.getInodeCache().remove(file.getNodeId());
			new RecordFile(scope, file.getFile());
		}
		return this;
	}
	
	// enforces that after hard link operations, the changed CTIME of the master Inode is written back to the databas
	public FileCollection recalcMaster() throws IOException, SQLException {
		return recalc(_master);
	}

	public FileCollection recalcFile() throws IOException, SQLException {
		return recalc(_rfile);
	}
	
	protected FileCollection remove(RecordFile file) {
		if(_bValid && file != null) {
			log.debug("remove: file = " + file.getFile().getAbsolutePath());
			_files.remove(file.getFile());
		}
		return this;
	}
	
	public FileCollection removeMaster() throws IOException, SQLException {
		return remove(_master);
	}

	public FileCollection removeFile() throws IOException, SQLException {
		return remove(_rfile);
	}
	
	public FileCollection modify(IFuncConsume<FileCollection> func) throws IOException, SQLException {
		func.consume(this);
		return this;
	}
	
	public FileCollection execute(IFuncConsume<RecordFile> func) throws IOException, SQLException {
		for(RecordFile file : _files.values()) {
			func.consume(file);
		}
		return this;
	}
	
	public FileCollection addAll(Collection<RecordFile> files) {
		files.stream().forEach( f -> add(f) );
		return this;
	}
	
	public Collection<RecordFile> files() {
		return Collections.unmodifiableCollection(_files.values());
	}
	
	public FileCollection all() {
		return FileCollection.create().scope(_scope).main(this).bin(_bin).addAll(_files.values()).addAll(_bin.files());
	}

	/** Checks if the provided file is an <b>valid</b> xattr file.
	 * It is valid if:
	 * <ul>
	 * <li>The file name starts with "._"</li>
	 * <li>The content header reports an xattr file.</li>
	 * <li>The is a parent file with the corresponding file name.
	 * </ul>
	 * 
	 * This means that for example orphaned xattr files are not considered as valid.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean isXattrSidecar(File file) throws IOException {
		File parent = file.getParentFile();
		String sName = file.getName();
		
		// MacOS xattr files
		if(sName.startsWith(XATTR_PREFIX) && new File(parent, sName.substring(XATTR_PREFIX.length())).exists() && 
				CloseableAction.execute(new XattrFile(file), f -> f.isValid())) {
			return true;
		}
		
		return false;
	}
	
	public static File getXattrFile(File file) throws IOException {
		
		if(!file.exists())
			return null;
		
		File parent = file.getParentFile();
		String sName = "._" + file.getName();
		File xattrFile = new File(parent, sName);
		
		// MacOS xattr files
		if(CloseableAction.execute(new XattrFile(xattrFile), f -> f.isValid())) {
			return xattrFile;
		}
		
		return null;
	}
	
	public static XattrFile getXattr(File file) throws IOException {
		if(!file.exists())
			return null;
		
		File parent = file.getParentFile();
		String sName = "._" + file.getName();
		File xattrFile = new File(parent, sName);
		
		if(!xattrFile.exists())
			return null;
		
		return new XattrFile(xattrFile);
	}

	public static boolean isXmpSidecar(File file) {
		// XMP metadata files (for image files)
		if(!FileUtil.getExtension(file).equalsIgnoreCase(".xmp")) {
			return false;
		}
		
		return true;
		
		// TODO activate
		
		/*
		
		String sName = FileUtil.getNameWithoutExtension(file);
		
		StoredInteger nXmpCount = new StoredInteger();
		StoredInteger nMainCount = new StoredInteger();
		
		Arrays.stream(file.getParentFile().listFiles()).filter( f -> f.isFile() && !f.equals(file) && FileUtil.getNameWithoutExtension(f).equals(sName) ).forEach( f -> {
			if(FileUtil.getExtension(f).equals(".xmp")) {
				nXmpCount.increment();
			}
			else {
				nMainCount.increment();
			}
		});
		
		return nXmpCount.get() == 0 && nMainCount.get() == 1;
		
		*/
	}
	
	public static boolean isSidecar(File file) throws IOException {
		return isXattrSidecar(file) || isXmpSidecar(file);
	}


	public FileCollection addFile() {
		if(_file != null)
			if(_nMaximumFileSize != -1 && _file.length() < _nMaximumFileSize)
				_bin.add(_rfile);
			else
				add(_rfile);
		return this;
	}
	
	public FileCollection sort() {
		List<RecordFile> files = new ArrayList<>(_files.values());
		files.sort(new Comparator<RecordFile>() {

			@Override
			public int compare(RecordFile f1, RecordFile f2) {
				return f1.getFile().compareTo(f2.getFile());
			}
		});
		
		_files.clear();
		for(RecordFile file : files) {
			_files.put(file.getFile(), file);
		}
		
		return this;
	}
	

}
