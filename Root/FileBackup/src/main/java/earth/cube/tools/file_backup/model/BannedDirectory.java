package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileName;
import earth.cube.tools.file_backup.commons.FileRelocator;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.ValidationException;
import earth.cube.tools.file_backup.database.to_be_checked.RecordBannedDirectoryFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class BannedDirectory {
	
	public final static String REGISTER_ACTION = "BannedDirectory:Registration";

	public static final String DELETE_ACTION = "BannedDirectory:Deletion";

	public final static String DIR_TOKEN = "/@DeleteDirectory/";

	
	protected Map<String,RecordBannedDirectoryFile> _files = new HashMap<>();
	protected List<RecordBannedDirectoryFile> _triggers = new ArrayList<>();
	
	@Getter @Setter
	protected long _nId;
	
	@Getter
	protected File _baseDirectory;
	
	@Getter @Setter
	protected String _sRemark;
	
	protected String _test;
	
	protected Optional<Boolean> _matches = Optional.empty();

	private boolean _bNew;
	
	@Getter
	protected boolean _bValid;

	private Scope _scope;

	private RecordFile _anyFile;

	
	public BannedDirectory(Scope scope, RecordFile anyFile) {
		_scope = scope;
		_nId = System.currentTimeMillis();
		_anyFile = anyFile;
		
		String sPath = anyFile.getFile().getAbsolutePath();
		int i = sPath.indexOf(DIR_TOKEN);
		if(i == -1)
			return;
		
		String sBaseDir = sPath.substring(0, i + DIR_TOKEN.length());
		sPath = sPath.substring(i + DIR_TOKEN.length());
		String[] sa = sPath.split("/", 2);
		_sRemark = sa[0];
		sBaseDir += '/' + sa[0];
		_baseDirectory = new File(sBaseDir);
		_bNew = true;
		_bValid = true;
	}

	public BannedDirectory(Scope scope, long nId, File baseDir) {
		_scope = scope;
		_nId = nId;
		_baseDirectory = baseDir;
		_bValid = true;
	}
	
	private void register(File dir) throws SQLException, IOException {
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				register(file);
			}
			else {
				// avoid StackOverflow / endless loop
				if(!FileCollection.isXattrSidecar(file)) {
					RecordBannedDirectoryFile bannedFile = _anyFile.getFile().equals(file) ? new RecordBannedDirectoryFile(_scope, this, _anyFile) :  new RecordBannedDirectoryFile(_scope, this, file);
					if(bannedFile.isValid()) {
						bannedFile.setRemark(_sRemark);
						add(bannedFile);
					}
				}
			}
		}
	}

	private void save() throws IOException, SQLException, ValidationException {
		if(_triggers.size() == 0) {
			throw new ValidationException("Missing Delete Triggers for directory '" + _baseDirectory.getAbsolutePath() + "'!");
		}
		
		for(RecordBannedDirectoryFile file : _files.values()) {
			file.save();
		}
	}

	public void register() throws SQLException, IOException, ValidationException {
		if(!_bNew)
			throw new IllegalStateException();
		register(_baseDirectory);
		save();
	}
	


	public RecordBannedDirectoryFile add(RecordBannedDirectoryFile file) {
		RecordBannedDirectoryFile curr = _files.get(file.getRelativePath());
		if(curr == null) {
			_files.put(file.getRelativePath(), file);
			if(file.isTrigger()) {
				_triggers.add(file);
			}
			curr = file;
		}
		return curr;
	}


	public boolean isMatching() throws IOException, SQLException {
		if(!_matches.isPresent()) {
			boolean bMatches = true;
			for(RecordBannedDirectoryFile file : _triggers) {
				bMatches &= file.isMatching();
			}
			log.debug("isMatching: dir = {}, id = {}, is matching = {}", _baseDirectory.getAbsolutePath(), _nId, bMatches);
			_matches = Optional.of(bMatches);
		}
		return _matches.get();
	}
	
	
	public void delete() throws IOException, SQLException {
		if(_bNew) {
			for(RecordBannedDirectoryFile file : _files.values()) {
				file.getFile().chain().delete();
			}
			return;
		}
		
		if(!isMatching()) {
			return;
		}

		log.debug("delete: dir = {}, id = {}", _baseDirectory.getAbsolutePath(), _nId);
		
		File targetDir = FileUtil.relocate(_scope.getRootDirectory(), _scope.getDeletionsDirectory(), _baseDirectory);
		targetDir = new FileName(targetDir).findUniqueFileName();
		
		FileRelocator relocator = new FileRelocator(_baseDirectory, targetDir, true);

		List<String> purged = new ArrayList<>();
		for(RecordBannedDirectoryFile file : _files.values()) {
			if(file.isMatching()) {
				purged.add(file.getRelativePath());   // TODO deleted sidecars are not listed
				file.getFile().bundle().relocator(relocator).delete();  
			}
		}
		
		Collections.sort(purged);
		
		File purgedFile = new File(_baseDirectory, "purged.txt");
		try(PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(purgedFile, true), StandardCharsets.UTF_8))) {
			out.println("# id:     " + _nId);
			out.println("# remark: " + _sRemark);
			for(String sPath : purged) {
				out.println(sPath);
			}
		}
		
	}

	public RecordBannedDirectoryFile getAnyTrigger() {
		return _triggers.size() == 0 ? null : _triggers.get(0);
	}


}
