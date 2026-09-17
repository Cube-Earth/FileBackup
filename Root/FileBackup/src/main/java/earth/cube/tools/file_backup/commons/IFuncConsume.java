package earth.cube.tools.file_backup.commons;

import java.io.IOException;
import java.sql.SQLException;

public interface IFuncConsume<T> {
	
	void consume(T data) throws IOException, SQLException;

}
