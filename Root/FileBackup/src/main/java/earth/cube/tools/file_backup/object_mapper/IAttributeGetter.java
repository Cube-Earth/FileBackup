package earth.cube.tools.file_backup.object_mapper;

public interface IAttributeGetter {

	AttributeType getType();

	Object get(Object instance) throws Exception;

}