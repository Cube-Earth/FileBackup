package earth.cube.tools.file_backup.commons;

public class TestCaseValidationException extends AssertionError {
	
	private static final long serialVersionUID = 1L;
	
	private ValidationError _error;

	public TestCaseValidationException(ValidationError error) {
		super(error.toString());
		_error = error;
	}
	
	public TestCaseValidationException(ValidationError error, String sMessage) {
		super(error + ": " + sMessage);
		_error = error;
	}

	public ValidationError getError() {
		return _error;
	}
	

}
