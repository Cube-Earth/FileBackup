package earth.cube.tools.file_backup.commons;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import earth.cube.libs.testings.assertions.DateWrapper;
import earth.cube.tools.file_backup.Scope;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Accessors(fluent=true)
public class DatabaseDumper {
	
	public static DatabaseDumper from(Scope scope) {
		return new DatabaseDumper(scope);
	}


	private Scope _scope;
		
	@Setter
	private String _sQuery;

	private List<String> _attrs;
	
	private List<Integer> _types;
	
	private int _nAttrCount;

	private List<List<String>> _resultSet;

	private List<StoredInteger> _sizes;
	
	
	public DatabaseDumper(Scope scope) {
		_scope = scope;
	}
	
	private int convertType(int nType) {
		int nConvertedType;
		switch(nType) {
		
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.BIGINT:
			case Types.REAL:
				nConvertedType = Types.NUMERIC;
				break;
				
			case Types.VARCHAR:
				nConvertedType = Types.CHAR;
				break;
				
			case Types.TIMESTAMP:
				nConvertedType = Types.TIMESTAMP;
				break;
				
			default:
				throw new IllegalArgumentException(Integer.toString(nType));
		
		}
		return nConvertedType;
	}
	
	private void extractColumns(ResultSet rs) throws SQLException {
		_attrs = new ArrayList<>();
		_types = new ArrayList<>();
		ResultSetMetaData metadata = rs.getMetaData();
		_nAttrCount = metadata.getColumnCount();
		for(int i = 1; i <= _nAttrCount; i++) {
			_attrs.add(metadata.getColumnLabel(i));
			_types.add(convertType(metadata.getColumnType(i)));
		}
	}
	

	private void calculateSizes() {
		_sizes = new ArrayList<>();
		
		for(int i = 0; i < _nAttrCount; i++) {
			_sizes.add(new StoredInteger(_attrs.get(i).length()));
		}
		
		int nRows = _resultSet.size();
		for(int i = 0; i < nRows; i++) {
			List<String> row = _resultSet.get(i);
			for(int j = 0; j < _nAttrCount; j++) {
				String s = row.get(j);
				_sizes.get(j).max(s.length());
			}
			
		}
		
	}
	
	
	private String repeat(char c, int n) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n; i++)
			sb.append(c);
		return sb.toString();
	}

	private String getCellName(int nColIdx) {
		String s = _attrs.get(nColIdx);
		int n = _sizes.get(nColIdx).get();
		String p = repeat(' ', n - s.length());
		return s + p;
	}

	
	private String getCellValue(List<String> row, int nColIdx) {
		String s = row.get(nColIdx);
		int n = _sizes.get(nColIdx).get();
		String p = repeat(' ', n - s.length());
		s = (_types.get(nColIdx) == Types.NUMERIC) ? p + s : s + p;
		return s;
	}


	private void printResultSet() {
		StringBuilder sb = new StringBuilder();

		// header
		for(int i = 0; i < _nAttrCount; i++) {
			if(i > 0) {
				sb.append(" | ");
			}
			sb.append(getCellName(i));
		}
		log.debug(sb.toString());
		sb = new StringBuilder();
		
		// line
		for(int i = 0; i < _nAttrCount; i++) {
			if(i > 0) {
				sb.append("-+-");
			}
			sb.append(repeat('-', _sizes.get(i).get()));
		}
		log.debug(sb.toString());
		sb = new StringBuilder();		

		// rows
		for(List<String> row : _resultSet) {
			for(int i = 0; i < _nAttrCount; i++) {
				if(i > 0) {
					sb.append(" | ");
				}
				sb.append(getCellValue(row, i));
			}
			log.debug(sb.toString());
			sb = new StringBuilder();
		}

	}
	
	public void execute() throws IOException, SQLException {
		_resultSet = new ArrayList<>();
		
		Query.perform(_scope.getConnection(), q -> q.query(_sQuery).execute().process( rs -> {

			if(_attrs == null) {
				extractColumns(rs);
			}
			
			List<String> row = new ArrayList<>();
			String sValue;
			
			for(int i = 0; i < _nAttrCount; i++) {
				switch(_types.get(i)) {

					case Types.TIMESTAMP:
						sValue = new DateWrapper(rs.getTimestamp(i+1)).toString();
						break;
						
					default:
						sValue = rs.getString(i+1);
				}
				
				row.add(sValue);
			}
			
			_resultSet.add(row);
		}));		

		calculateSizes();
		printResultSet();
	}
	
	
	public void dumpCompoundFiles() throws IOException, SQLException {
		query("select f.PATH, f.INODE, f.MODIFIED, f.VALID, f.UPDATED as f_updated, n.CTIME, n.SIZE, n.EXTENSION, n.SHA256, n.SHA256_UPDATED, n.UPDATED as n_updated from file f, inode n where f.inode=n.id").execute();
	}
	


}
