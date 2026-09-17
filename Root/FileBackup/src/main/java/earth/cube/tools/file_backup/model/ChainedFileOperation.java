package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.FileCollection;
import earth.cube.tools.file_backup.commons.FileRelocator;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.StoredObject;
import earth.cube.tools.file_backup.database.RecordBannedFile;
import earth.cube.tools.file_backup.database.to_be_checked.RecordBannedDirectoryFile;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ChainedFileOperation {
	
	protected Scope _scope;

	@Getter
	protected RecordFile _rfile;

	protected File _file;

	
	public ChainedFileOperation(RecordFile file) {
		_rfile = file;
		_file = file.getFile();
		_scope = file.getScope();
	}
	
	public ChainedFileOperation(Scope destinationScope, RecordFile file) {
		_rfile = file;
		_file = file.getFile();
		_scope = destinationScope;
	}
	
	public void delete() throws IOException, SQLException {
		_rfile.bundle().delete();
	}
	
	
	public void delete(FileRelocator relocator) throws IOException, SQLException {
		_rfile.bundle().delete(relocator);
	}
	

	public void archive(boolean bDelete) throws IOException, SQLException {
		_rfile.bundle().archive(bDelete);
	}

	public void archive() throws IOException, SQLException {
		_rfile.bundle().archive();
	}
		
	
	public void ban(String sRemark) throws IOException, SQLException {
		FileCollection.create().file(_rfile).addFile()
		.execute( f -> {
			log.debug("ban: registering file = " + f.getFile().getAbsolutePath());
			RecordBannedFile record = new RecordBannedFile(_scope, f.getFile(), sRemark);
			record.save();
		})
		.bin().addFile().addFilesWithSameSha256().execute( f -> {
			// 'ban' is invoked before the appropriate file is stored in the database. So use 'addFile' to add current RecordFile instance to delete

			log.debug("ban: deleting file = " + f.getFile().getAbsolutePath());
			f.chain().delete();
		});
	}
	
	
	/**
	 * 
	 * @param nMinimumFileSize The minimum file size at which files are deduplicated (hard linked). -1 will be replaced by a default value. Use 0, if all files should be deduplicated.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void deduplicate(int nMinimumFileSize) throws IOException, SQLException {
		StoredObject<RecordFile> master = new StoredObject<>();
		FileCollection.create().file(_rfile).maximumFileSize(nMinimumFileSize == -1 ? 1_000_000 : nMinimumFileSize).addFilesWithSameSha256().removeMasterInode().storeMaster(master)
		.execute( f -> {
			log.debug("deduplicate: hard linking file = " + f.getFile().getAbsolutePath());
			File orgFile = f.getFile();
			f.bundle().archive();
			Files.createLink(orgFile.toPath(), master.getObject().getFile().toPath());
			new RecordFile(_scope, orgFile);   // update database
		
		}).recalcMaster();

	}	
	

	public void copyTo(File srcDir, File dstDir) throws IOException, SQLException {
		FileRelocator relocator = new FileRelocator(srcDir, dstDir, true);
		FileCollection.create().scope(_scope).file(_rfile).maximumFileSize(0x2_000_000).addMasterWithSameSha256().addFileIfMasterExists()
			.execute( f -> {
				log.debug("copyTo: hard linking file = " + f.getFile().getAbsolutePath());
				File dstFile = relocator.relocate(f.getFile());
				Files.createLink(dstFile.toPath(), f.getFile().toPath());
			}).recalcMaster()
			.bin().addXattr().addUnpreciseXmp().addFileIfMasterExists().execute( f -> {
				log.debug("copyTo: copying file = " + f.getFile().getAbsolutePath());
				File dstFile = relocator.relocate(f.getFile());
				Files.copy(f.getFile().toPath(), dstFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			});
		
	}

	
	public void moveTo(File srcDir, File dstDir, PrintStream out) throws IOException, SQLException {
		FileRelocator relocator = new FileRelocator(srcDir, dstDir, true);
		
		FileCollection.create().scope(_scope).file(_rfile).maximumFileSize(0x1_000_000).addMasterWithSameSha256().addFileIfMasterExists()
			.execute( f -> {
				log.debug("moveTo: hard linking file = " + f.getFile().getAbsolutePath());
				File dstFile = relocator.relocate(f.getFile());
				Files.createLink(dstFile.toPath(), f.getFile().toPath());
				out.println(f.getCalculatedSha256FromInode() + "   " + relocator.getRelativePath(f.getFile()));
			}).recalcMaster()
			.bin().addXattr().addUnpreciseXmp().addFileIfMasterExists().execute( f -> {
				log.debug("moveTo: copying file = " + f.getFile().getAbsolutePath());
				File dstFile = relocator.relocate(f.getFile());
				Files.copy(f.getFile().toPath(), dstFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		
				// ok, a bit inconsistent/confusing:
				// - this function identifies the sidecar files and copies them
				// - the delete function ignores the deletion for sidecar files but extends the
				//   main file deletion by the sidecars.
				// (so everything is ok at the end)
				// maybe create also a copy/move function in the BundledFileOperations and simplify
				// this code.
				f.chain().delete();
			});
	}

	public void removeOtherDuplicates(File srcDir) throws IOException, SQLException {
		log.debug("removeOtherDuplicates: file = " + _file.getAbsolutePath());
		
		Set<Long> inodes = new HashSet<>(); // TODO needed?

		FileCollection.create().file(_rfile).addFilesWithSameSha256().removeFile()
			.execute(f -> {
				if(FileUtil.getRelativePath(srcDir, f.getFile()) == null) {
					inodes.add(f.getNodeId());
					f.chain().archive(true);
				}
			});

		// enforce that new CTIME of INODE is written back to database
		RecordFile.updateInodes(_scope, inodes);
	}

	public void removeAllOtherDuplicates() throws IOException, SQLException {
		log.debug("removeAllOtherDuplicates: file = " + _file.getAbsolutePath());
		
		Set<Long> inodes = new HashSet<>(); // TODO needed?

		FileCollection.create().file(_rfile).addFilesWithSameSha256().removeFile()
			.execute(f -> {
				inodes.add(f.getNodeId());
				f.chain().archive(true);
			});

		// enforce that new CTIME of INODE is written back to database
		RecordFile.updateInodes(_scope, inodes);
	}

	public void removeTheseDuplicates() throws IOException, SQLException {
		log.debug("removeTheseDuplicates: file = " + _file.getAbsolutePath());
		
		Set<Long> inodes = new HashSet<>(); // TODO needed?

		if(FileCollection.create().file(_rfile).addFilesWithSameSha256().removeFile().files().size() > 0) {
			inodes.add(_rfile.getNodeId());
			_rfile.chain().archive(true);
		}

		// enforce that new CTIME of INODE is written back to database
		RecordFile.updateInodes(_scope, inodes);
	}

	public void banDirectory(BannedDirectory bannedDir) throws IOException, SQLException {
		log.debug("banDirectory: file = " + _file.getAbsolutePath());
		log.debug("banDirectory: id = " + bannedDir.getId());

		RecordBannedDirectoryFile trigger = bannedDir.getAnyTrigger();
		if(trigger != null) {
			log.debug("banDirectory: '{}' is a trigger ", trigger.getRelativePath());
			FileCollection.create().scope(_scope).sha256(trigger).addFilesWithSameSha256().execute( f -> {
				if(f.isValid()) {
					log.debug("banDirectory: '{}' is not matching", f.getNeutralPath());
				}
				else {
					log.debug("banDirectory: '{}' has been deleted", f.getNeutralPath());
				}
			});
		}
	}
	
}
