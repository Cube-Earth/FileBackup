package earth.cube.tools.file_backup.commons;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

public class CloseableAction {
	
	public static <T extends Closeable, U> U execute(T obj, IFuncFunction<T, U> func) throws IOException {
		try {
			try {
				return func.apply(obj);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
		finally {
			obj.close();
		}
	}

	public static <T extends Closeable, U> U execute(T obj, IFuncFunction<T, U> func, U defaultValue) throws IOException {
		return obj == null ? defaultValue : execute(obj, func);
	}

}
