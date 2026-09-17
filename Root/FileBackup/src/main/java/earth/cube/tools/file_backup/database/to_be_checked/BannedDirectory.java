package earth.cube.tools.file_backup.database.to_be_checked;

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
import earth.cube.tools.file_backup.commons.FileName;
import earth.cube.tools.file_backup.commons.FileRelocator;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.ValidationException;
import earth.cube.tools.file_backup.config.VolumeConfig;
import earth.cube.tools.file_backup.filesystem.CompoundFile;
import earth.cube.tools.file_backup.model.RecordFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class BannedDirectory {
		
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

	private Scope _scopex;
	
	@Getter
	protected VolumeConfig _config;

	
	public BannedDirectory(VolumeConfig config, File baseDirectory) {
		_config = config;
		
		_baseDirectory = baseDirectory;
		_nId = System.currentTimeMillis();
		_bNew = true;
		_bValid = true;
	}

	public BannedDirectory(VolumeConfig config, long nId, File baseDir) {
		_config = config;
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
				CompoundFile compoundFile = new CompoundFile(_config.getFileSystem(), file);
				if(compoundFile.isValid()) {
					RecordBannedDirectoryFile bannedFile = new RecordBannedDirectoryFile(_scope, this, file);
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
