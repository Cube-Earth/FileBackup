package earth.cube.tools.file_backup.commons;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;

public final class PositionedInputStream extends FilterInputStream {

	@Getter
	protected long _nPosition = 0;

	protected long _nMarker = 0;
	

	public PositionedInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b != -1)
			_nPosition++;
		return b;
	}

	@Override
	public int read(byte[] buf, int nOffset, int nLength) throws IOException {
		int n = super.read(buf, nOffset, nLength);
		_nPosition += n;
		return n;
	}

	@Override
	public long skip(long nSkipBytes) throws IOException {
		long n = super.skip(nSkipBytes);
		_nPosition += n;
		return n;
	}

	@Override
	public void mark(int nReadLimit) {
		super.mark(nReadLimit);
		_nMarker = _nPosition;
	}

	@Override
	public void reset() throws IOException {
		if (!markSupported())
			throw new IOException("Mark not supported!");
		super.reset();
		_nPosition = _nMarker;
	}

}