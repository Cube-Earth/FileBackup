package earth.cube.tools.file_backup.commons;

import java.io.IOException;
import java.sql.SQLException;

public interface IFuncBiFunction<T, U, V> {
	
	V apply(T data1, U data2) throws IOException, SQLException;

}
