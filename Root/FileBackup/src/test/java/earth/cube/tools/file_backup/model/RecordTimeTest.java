package earth.cube.tools.file_backup.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.TestDataDirectory;
import earth.cube.tools.file_backup.commons.TestDataDirectoryBuilder;

public class RecordTimeTest {
	
	protected final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	
	protected TestDataDirectory _testDataDir;
	private File _rootDir;
	private Scope _scope;
	
	@BeforeEach
	public void setUp(TestInfo info) throws IOException, SQLException {
		_testDataDir = TestDataDirectoryBuilder.instance(info).build();
		_rootDir = _testDataDir.getDirectory();
		assertEquals(1, FileUtil.countFiles(_rootDir)); // database file
		assertEquals(0, _testDataDir.getDatabaseFile().length());
		_scope = new Scope(_rootDir);
		ensureTable(_scope.getConnection());
		System.out.println("=== " + info.getDisplayName() + " ==============================");
	}
		

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "TIME")) {
	    	String sSql = "CREATE TABLE TIME " +
	                 "(ID         TINYINT    PRIMARY KEY    NOT NULL," +
	    			  "TIME       real                  NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
		}
	    else
	    	fail();
	}
	

	
	@Test
	public void test_1() throws IOException, SQLException {
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 11, 5, 12, 30, 15);
		cal.set(Calendar.MILLISECOND, 475);
		Date date1 = cal.getTime();
		
		System.out.println(date1);
		
		String sSql = String.format("INSERT OR REPLACE INTO TIME (ID, TIME) VALUES (0, %s)", Query.getJulianDate(date1));
		System.out.println("sql = " + sSql);
		
		Query.perform(_scope.getConnection(), q -> q.query("INSERT OR REPLACE INTO TIME (ID, TIME) VALUES (0, %s)", Query.getJulianDate(date1)).execute());
		Date date2 = Query.perform(_scope.getConnection(), q -> q.query("select TIME as t from time").execute().asDate("t"));
		
		assertEquals(date1, date2, "expected: " + DF.format(date1) + ", actual: " + DF.format(date2));
	}
	
	

}
