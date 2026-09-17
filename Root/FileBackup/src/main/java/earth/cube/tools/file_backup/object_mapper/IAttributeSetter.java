package earth.cube.tools.file_backup.object_mapper;

public interface IAttributeSetter {

	AttributeType getType();

	void set(Object instance, Object value) throws Exception;

}