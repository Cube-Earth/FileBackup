package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import earth.cube.libs.testings.assertions.DateWrapper;

public class AssertUtils {
	
	public static void assertValidationThrows(ValidationError error, Executable exec) {
		try {
			exec.execute();
		} catch (Throwable e) {
			if(e instanceof AssertionFailedError) {
				AssertionFailedError ee = (AssertionFailedError) e;
				if(ee.getCause() instanceof TestCaseValidationException) {
					assertEquals(error, ((TestCaseValidationException) ee.getCause()).getError());
					return;
				}
				else
					throw (AssertionFailedError) e;
			}
			else
				if(e instanceof TestCaseValidationException) {
					assertEquals(error, ((TestCaseValidationException) e).getError());
					return;
				}
				else
					fail(e.getClass().getCanonicalName(), e);
		}
		fail("No exception has been thrown!");
	}
	
	public static void assertValidationEquals(Object expected, Object actual, ValidationError error) {
		if(expected != actual && (expected != null && !expected.equals(actual)))
			assertionFailure()
			.message(null)
			.cause(new TestCaseValidationException(error))
			.expected(expected instanceof Date ? new DateWrapper((Date) expected) : expected)
			.actual(actual instanceof Date ? new DateWrapper((Date) actual) : actual)
			.buildAndThrow();
	}

}
