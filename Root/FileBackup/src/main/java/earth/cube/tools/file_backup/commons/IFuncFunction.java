package earth.cube.tools.file_backup.commons;

import java.io.IOException;
import java.sql.SQLException;

public interface IFuncFunction<T, U> {
	
	U apply(T data) throws IOException, SQLException;

}
