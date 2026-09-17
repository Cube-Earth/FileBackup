package earth.cube.tools.file_backup.commons;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.TestInfo;

public class TestDataDirectoryBuilder {
	
	private File _dir;
	
	
	public static TestDataDirectoryBuilder instance(Class<?> clazz) {
		return new TestDataDirectoryBuilder().clazz(clazz);
	}
	
	public static TestDataDirectoryBuilder instance(TestInfo info) {
		return new TestDataDirectoryBuilder().testCase(info);
	}
	
	public TestDataDirectoryBuilder() {
		_dir = new File("test-ouput");
	}
	
	public TestDataDirectoryBuilder clazz(Class<?> clazz) {
		_dir = new File(_dir, clazz.getCanonicalName());
		return this;
	}

	public TestDataDirectoryBuilder methodName() {
		_dir = new File(_dir, TestUtil.getMethodName(2));
		return this;
	}

	public TestDataDirectoryBuilder methodName(int nCaller) {
		_dir = new File(_dir, TestUtil.getMethodName(nCaller + 2));
		return this;
	}
	
	public TestDataDirectoryBuilder folder(String sFolder) {
		_dir = new File(_dir, sFolder);
		return this;
	}

	public TestDataDirectoryBuilder testCase(TestInfo info) {
		_dir = new File(_dir, info.getTestClass().get().getCanonicalName() + '/' + info.getTestMethod().get().getName());
		return this;
	}

	public TestDataDirectoryBuilder random() {
		_dir.mkdirs();
		try {
			_dir = File.createTempFile("", "", _dir);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		_dir.delete();
		return this;
	}
	
	public TestDataDirectory build() throws IOException {
		if(_dir.exists()) {
			FileUtil.deleteDirectory(_dir);
		}
		_dir.mkdirs();
		return new TestDataDirectory(_dir);
	}


}
