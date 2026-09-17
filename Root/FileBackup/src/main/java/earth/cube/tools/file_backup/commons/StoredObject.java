package earth.cube.tools.file_backup.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class StoredObject<T> {
	
	@Getter @Setter
	private T _object;
	

}
