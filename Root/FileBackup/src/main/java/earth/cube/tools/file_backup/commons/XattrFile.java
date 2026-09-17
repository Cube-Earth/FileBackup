package earth.cube.tools.file_backup.commons;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

import lombok.Getter;

public class XattrFile implements Closeable {
	
	protected File _file;

	protected BufferedInputStream _bis;
	
	protected PositionedInputStream _pis;
	
	protected DataInputStream _dis;
	
	protected byte[] buf = new byte[4096];

	
	@Getter
	protected boolean _bValid;

	protected int _nHeaderSize;

	protected int _nDataSize;

	protected int _nFileSize;

	protected int _nUnusedSize;
	
	protected AttributesHeader _attrsHeader;
	
	protected Map<String,AttributeEntry> _attrs;
	
	protected boolean _bValuesLoaded;
	
	protected Set<String> _tags;
	protected Set<String> _loweredTags;


	protected class AttributesHeader {
		
		@Getter
		protected int _nAttributeCount;

		protected void read() throws IOException {
			_dis.skipBytes(0x45);
			_nAttributeCount = _dis.read();
		}
		
	}
	
	protected class AttributeEntry {
		
		protected int _nAttrOffset;
		
		@Getter
		protected int _nValueOffset;
		
		protected int _nValueSize;
		protected int _nNameSize;

		@Getter
		protected String _sName;
		
		@Getter
		private byte[] _value;
		
		public void read() throws IOException {
			_nAttrOffset = (int) _pis.getPosition();
			_nValueOffset = _dis.readInt();
			_nValueSize = _dis.readInt();
			_dis.readShort(); // unknown
			_nNameSize = _dis.read();  // including null-termination
			_dis.readFully(buf, 0, _nNameSize);
			_sName = new String(buf, 0, _nNameSize - 1, StandardCharsets.UTF_8);
			
			int n = (11 + _nNameSize) % 8;
			if(n != 0) {			
				_dis.skipBytes(8 - n);
			}
		}
		
		public String getDump() {
			return String.format("%1$s:\n" +
					"attribute offset: %2$s (0x%2$h)\n" +
					"name      size  : %3$s (0x%3$h)\n" +
					"value     offset: %4$s (0x%4$h)\n" +
					"value     size  : %5$s (0x%5$h)\n",
					_sName,
					_nAttrOffset,
					_nNameSize,
					_nValueOffset,
					_nValueSize
					);
		}
		
		public void loadValue() throws IOException {
			if(_pis.getPosition() > _nValueOffset)
				throw new IllegalStateException();
			
			_dis.skipBytes(_nValueOffset - (int) _pis.getPosition());
			_value = new byte[_nValueSize];
			_dis.readFully(_value);
		}
		
	}

	
	
	public XattrFile(File file) throws IOException {
		_file = file;
		_bis = new BufferedInputStream(new FileInputStream(file));
		_pis = new PositionedInputStream(_bis);
		_dis = new DataInputStream(_pis);
		readFileHeader();
	}
	
	@Override
	public void close() throws IOException {
		_dis = null;
		_pis = null;
		_bis.close();
		_bis = null;
	}
	
	protected void readFileHeader() throws IOException {
		_bValid = false;
		
		// file is in big-endian order
		
		// magic
		if(_dis.readInt() != 0x00051607) {
			return;	
		}
		
		// version
		if(_dis.readShort() != 0x2) {
			return;
		}
		
		// reserved (usually zero)
		_dis.readShort();

		_dis.readFully(buf, 0, 16);
		if(!new String(buf, 0, 16, StandardCharsets.ISO_8859_1).equals("Mac OS X        ")) {
			return;
		}

		// unknown
		_dis.readShort();
		_dis.readInt();
		
		_nHeaderSize = _dis.readInt();
		_nDataSize = _dis.readInt();
		_dis.readInt(); // unknown
		_nFileSize = _dis.readInt();
		_nUnusedSize = _dis.readInt();
		
		assert _nHeaderSize + _nDataSize == _nFileSize;
		assert _file.length() == _nFileSize + _nUnusedSize;
		assert _pis.getPosition() == _nHeaderSize;
		
		_bValid = true;
	}
	

	protected void readAttributes() throws IOException {
		if(!_bValid) {
			throw new IllegalStateException("File is not valid!");
		}
		
		 _attrs = new HashMap<>();
		_attrsHeader = new AttributesHeader();
		_attrsHeader.read();
		
		int n = _attrsHeader.getAttributeCount();
		for(int i = 0; i < n; i++) {
			AttributeEntry attr = new AttributeEntry();
			attr.read();
			_attrs.put(attr.getName(), attr);
		}
	}

	protected void loadValues() throws IOException {
		if(!_bValuesLoaded) {
			if(_attrs == null) {
				readAttributes();
			}
			
			List<AttributeEntry> attrs = new ArrayList<>(_attrs.values());
			attrs.sort(new Comparator<AttributeEntry> () {

				@Override
				public int compare(AttributeEntry a1, AttributeEntry a2) {
					return Integer.compare(a1.getValueOffset(), a2.getValueOffset());
				}
				
			});
			
			
			for(AttributeEntry attr : attrs) {
				attr.loadValue();
			}
			
			_bValuesLoaded = true;
		}		
	}

	protected void loadTags() throws IOException {
		if(!_bValuesLoaded) {
			loadValues();
		}
		_tags = new HashSet<>();
		_loweredTags = new HashSet<>();
		
		AttributeEntry attr = _attrs.get("com.apple.metadata:_kMDItemUserTags");
		
		if(attr != null) {
			try {
				NSArray arr = (NSArray) PropertyListParser.parse(attr.getValue());
				
				int n = arr.count();
				NSObject[] objs = arr.getArray();
				for(int i = 0; i < n; i++) {
					NSString str = (NSString) objs[i];
					String[] saTag = str.getContent().split("\n", 2); // second line is color code of tag
					_tags.add(saTag[0]);
					_loweredTags.add(saTag[0].toLowerCase());
				}
				
			} catch (IOException | PropertyListFormatException | ParseException | ParserConfigurationException
					| SAXException e) {
				throw new IOException(e);
			}
		}
	}

	
	public boolean hasTag(String... saTag) throws IOException {
		if(_tags == null) {
			loadTags();
		}
		return Arrays.asList(saTag).stream().anyMatch(s -> _tags.contains(s));
	}

	public boolean hasLoweredTag(String... saTag) throws IOException {
		if(_tags == null) {
			loadTags();
		}
		return Arrays.asList(saTag).stream().anyMatch(s -> _loweredTags.contains(s.toLowerCase()));
	}

	public void dump() throws IOException {
		if(_attrs == null) {
			readAttributes();
		}	
	}
	
	public Set<String> getAllTags() {
		return Collections.unmodifiableSet(_tags);
	}
	
	
	public static File getSidecarFile(File file) throws IOException {
		return FileCollection.getXattrFile(file);
	}

	public static XattrFile getSidecar(File file) throws IOException {
		return FileCollection.getXattr(file);
	}
	
	public static void main(String[] sArgs) throws IOException {
		XattrFile f = new XattrFile(new File("conf.xattr"));
		System.out.println(f.isValid());

		System.out.println(f.hasTag("Test"));
		
		f.getAllTags().forEach(s -> System.out.println("#" + s));
	}

	

}
