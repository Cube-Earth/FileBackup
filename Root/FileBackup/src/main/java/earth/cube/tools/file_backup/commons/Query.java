package earth.cube.tools.file_backup.commons;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class Query implements Closeable {
	
	private final static SimpleDateFormat DF_DT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private Connection _connection;
	private String _sSql;
	private Statement _stmt;
	private ResultSet _rs;
	private int _nRowMatched;
	
	protected Query(Connection connection) {
		_connection = connection;
	}
	
	public Query query(String sQuery, Object... args) {
		_sSql = String.format(sQuery, args);
		return this;
	}
	
	public Query execute() throws SQLException {
		_stmt = _connection.createStatement();
		if(_sSql.toLowerCase().trim().startsWith("select ")) {
			_rs = _stmt.executeQuery(_sSql);
		}
		else {
			_stmt.executeUpdate(_sSql);
		}
		return this;
	}
	
	public Query cache() throws SQLException {
		RowSetFactory factory = RowSetProvider.newFactory();
		CachedRowSet crs = factory.createCachedRowSet();
		crs = CachedRowSetProxy.newInstance(crs);
		crs.populate(_rs);
		_rs.close();
		_rs = crs;
		return this;
	}
	
	public String asString(String sAttr) throws SQLException {
		if(_nRowMatched == 1)
			return null;
		if(_nRowMatched == 0 && !_rs.next())
			throw new NoSuchElementException();
		return _rs.getString(sAttr);
	}
	
	public int asInt(String sAttr) throws SQLException {
		if(_nRowMatched == 1)
			return -1;
		if(_nRowMatched == 0 && !_rs.next())
			throw new NoSuchElementException();
		return _rs.getInt(sAttr);
	}
	
	public Date asDate(String sAttr) throws SQLException {
		if(_nRowMatched == 1)
			return null;
		if(_nRowMatched == 0 && !_rs.next())
			throw new NoSuchElementException();
		return _rs.getTimestamp(sAttr);
	}
	
	
	public Query processSingle(IFuncConsume<ResultSet> func) throws IOException, SQLException {
		_rs.next();
		func.consume(_rs);
		if(_rs.next())
			throw new IllegalStateException("Unexpected result rows!");
		return this;
	}

	public Query process(IFuncConsume<ResultSet> func) throws IOException, SQLException {
		while(_rs.next()) {
			func.consume(_rs);
		}
		return this;
	}

	public Query processUntil(IFuncFunction<ResultSet,Boolean> func) throws IOException, SQLException {
		_nRowMatched = 1;
		while(_rs.next()) {
			if(!func.apply(_rs)) {
				_nRowMatched = 2;
				break;
			}
		}
		return this;
	}

	@Override
	public void close() throws IOException {
		try {
			if(_rs != null)
				_rs.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
		finally {
			_rs = null;
			if(_stmt != null)
				try {
					_stmt.close();
				} catch (SQLException e) {
					throw new IOException(e);
				}
				finally {
					_stmt = null;
				}
		}
	}
	
	public static <T> T perform(Connection connection, IFuncFunction<Query,T> func) throws IOException, SQLException {
		try(Query q = new Query(connection)) {
			return func.apply(q);
		}
	}
	
	public static boolean existsTable(Connection connection, String sTableName) throws IOException, SQLException {
		String sSql = "SELECT count(*) as cnt FROM sqlite_master WHERE type='table' AND name='%s';";
		return Query.perform(connection, q -> q.query(sSql, sTableName).execute().asInt("cnt") > 0);
	}

	public static String getDateClause(Date date) {
		return String.format("datetime('%s')", DF_DT.format(date));
	}

	public static String getJulianDate(Date date) {
		return String.format("'%s'", DF_DT.format(date));
	}

	public static String getJulianDate(long nMillis) {
		return String.format("'%s'", DF_DT.format(new Date(nMillis)));
	}

}
