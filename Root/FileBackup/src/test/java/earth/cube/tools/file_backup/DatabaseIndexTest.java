package earth.cube.tools.file_backup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.actions.AbstractActionTest;
import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.model.InodeKey;

public class DatabaseIndexTest extends AbstractActionTest {
	
	@Override
	protected File getExistingDatabaseFile() throws IOException, SQLException {
		return new HugeDatabasePopulator("huge").getFile();
	}
	
	protected long executeQuery(String sQuery, Object... args) throws IOException, SQLException {
		long nStart = System.currentTimeMillis();
		Query.perform(_scope.getConnection(), q -> q.query(sQuery, args).execute());
		return System.currentTimeMillis() - nStart;
	}

/*	
	protected long queryFilePath(int i) throws IOException, SQLException {
		return executeQuery("select file where path='%s'", EscapeUtil.escapeSqlString(HugeDatabasePopulator.getFilePath(i)));
	}
*/
	
	protected long queryNodeId(int i) throws IOException, SQLException {
		return executeQuery("select file where inode=%s", HugeDatabasePopulator.getNodeId(i));
	}

	protected long queryNodeKey(int i) throws IOException, SQLException {
		InodeKey key = HugeDatabasePopulator.getNodeKey(i);
		return executeQuery("select inode where sha256='%s' and extensions='%s' and size=%s", EscapeUtil.escapeSqlString(key.getSha256()), EscapeUtil.escapeSqlString(key.getExension()), key.getSize());
	}
	
	@Test
	public void test_file_inode_1() throws IOException, SQLException {
		// TODO implement
		String sDropIdx = "DROP INDEX IF EXISTS idx_file_inode";
		String sCreateIdx = "CREATE UNIQUE INDEX idx_file_inode ON file (inode)";
		
		Query.perform(_scope.getConnection(), q -> q.query(sDropIdx).execute());
		
		long i = queryNodeId(HugeDatabasePopulator.NUM_INODES - 10);
		
		Query.perform(_scope.getConnection(), q -> q.query(sCreateIdx).execute());
		
		long j = queryNodeId(HugeDatabasePopulator.NUM_INODES - 10);
		
		System.out.println("#1 - " + i  + ", " + j);
		
		assertTrue(i / 10 > j, i + ", " + j);
	}

	@Test
	public void test_inode_key_1() throws IOException, SQLException {
		// TODO implement
		String sDropIdx = "DROP INDEX IF EXISTS idx_inode_key";
		String sCreateIdx = "CREATE UNIQUE INDEX idx_inode_key ON inode (sha256, extension, size)";
		
		Query.perform(_scope.getConnection(), q -> q.query(sDropIdx).execute());

		long i = queryNodeId(HugeDatabasePopulator.NUM_INODES - 10);
		
		Query.perform(_scope.getConnection(), q -> q.query(sCreateIdx).execute());
		
		long j = queryNodeId(HugeDatabasePopulator.NUM_INODES - 10);
		
		System.out.println("#2 - " + i  + ", " + j);

		assertTrue(i / 10 > j, i + ", " + j);
	}

}
