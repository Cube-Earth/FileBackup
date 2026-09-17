package earth.cube.tools.file_backup.commons;

public class ValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	
	public ValidationException() {
	}

	public ValidationException(String sMessage) {
		super(sMessage);
	}

	public ValidationException(Throwable t) {
		super(t);
	}

	public ValidationException(String sMessage, Throwable t) {
		super(sMessage, t);
	}
}
