package earth.cube.tools.file_backup.database;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.database.to_be_checked.RecordBannedDirectoryFile;
import earth.cube.tools.file_backup.model.RecordFile;
import earth.cube.tools.file_backup.model.RecordStatistics;

public class FileDatabase implements Closeable {

	private Connection _connection;
	private File _file;
	private int _nBatchId;
	
	public FileDatabase(File file, boolean bCreate) throws IOException {
		_file = file;
		if(!_file.exists()) {
			if(bCreate) {
				file.getParentFile().mkdirs();
				FileUtil.touch(file);
			}
			else
				throw new IllegalStateException("File '" + file.getAbsolutePath() + "' does not exists!");
		}
	}

	public void connect() throws IOException, SQLException {
		try {
	    Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
		
	    _connection = DriverManager.getConnection("jdbc:sqlite:" + _file.getAbsolutePath());
	    
	    RecordSequence.ensureTable(_connection);
	    RecordInode.ensureTable(_connection);
	    RecordFile.ensureTable(_connection);
	    RecordBannedFile.ensureTable(_connection);
	    RecordBannedDirectoryFile.ensureTable(_connection);
	    RecordStatistics.ensureTable(_connection);
	}
	
	

	@Override
	public void close() throws IOException {
		if(_connection != null)
			try {
				_connection.close();
				_connection = null;
			} catch (SQLException e) {
				throw new IOException(e);
			}
	}
	
	

	public Connection getConnection() throws SQLException, IOException {
		if(_connection == null) {
			connect();
			_nBatchId = new RecordSequence(_connection, "batch_id").acquireNext();
		}
		return _connection;
	}

	public int getBatchId() {
		return _nBatchId;
	}
	
	
}
