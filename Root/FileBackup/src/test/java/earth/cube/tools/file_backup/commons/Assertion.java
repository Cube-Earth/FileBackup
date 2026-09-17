package earth.cube.tools.file_backup.commons;

import java.util.Date;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.fail;

import earth.cube.libs.testings.assertions.DateWrapper;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent=true)
public class Assertion {
	
	protected Object _expected;
	protected boolean _bExpectedDate;

	protected Object _actual;
	protected boolean _bActualDate;
	
	@Setter
	protected String _sMessage;
	
	
	public static Assertion create() {
		return new Assertion();
	}
	
	public Assertion expected(Object expected) {
		if(expected instanceof Date) {
			_expected = new DateWrapper((Date) expected);
			_bExpectedDate = true;
		}
		else {
			_expected = expected;
			_bExpectedDate = false;
		}
		return this;
	}
	
	
	public Assertion actual(Object actual) {
		if(actual instanceof Date) {
			_actual = new DateWrapper((Date) actual);
			_bActualDate = true;
		}
		else {
			_actual = actual;
			_bActualDate = false;
		}
		return this;
	}
	
	public Assertion truncateMillis() {
		if(_bExpectedDate)
			((DateWrapper) _expected).truncateMillis();
		if(_bActualDate)
			((DateWrapper) _actual).truncateMillis();
		return this;
	}
	
	public void checkBefore() {
		if(_bExpectedDate && _bActualDate) {
			if(!((Date) _expected).before((Date) _actual))
				assertionFailure()
				.message(_sMessage)
				.expected(_expected)
				.actual(_actual)
				.buildAndThrow();
		}
		else
			fail();
	}

	public void checkBeforeOrEquals() {
		if(_bExpectedDate && _bActualDate) {
			if(!_expected.equals(_actual) && !((Date) _expected).before((Date) _actual))
				assertionFailure()
				.message(_sMessage)
				.expected(_expected)
				.actual(_actual)
				.buildAndThrow();
		}
		else
			fail();
	}

	public void checkAfterOrEquals() {
		if(_bExpectedDate && _bActualDate) {
			if(!_expected.equals(_actual) && !((Date) _expected).after((Date) _actual))
				assertionFailure()
				.message(_sMessage)
				.expected(_expected)
				.actual(_actual)
				.buildAndThrow();
		}
		else
			fail();
	}

	public void checkEquals() {
		if(!_actual.equals(_expected))
			assertionFailure()
			.message(_sMessage)
			.expected(_expected)
			.actual(_actual)
			.buildAndThrow();
	}

	

}
