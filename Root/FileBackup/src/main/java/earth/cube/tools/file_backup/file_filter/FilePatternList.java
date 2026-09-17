package earth.cube.tools.file_backup.file_filter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilePatternList implements IFilePattern {
	
	protected File _baseDir;
	protected FilePatternList _parent;
	protected List<IFilePattern> _patterns = new ArrayList<>();
	protected Map<String, IFilePattern> _tpls = new HashMap<>();
	
	public FilePatternList(File baseDir, FilePatternList parent) {
		_baseDir = baseDir;
		_parent = parent;
	}
	
	public void add(IFilePattern pattern) {
		_patterns.add(pattern);
	}

	public void addTemplate(String sName, IFilePattern pattern) {
		_patterns.add(pattern);
	}
	
	public IFilePattern getTemplate(String sName, boolean bRaiseError) {
		IFilePattern pattern = _tpls.get(sName);
		if(pattern == null && _parent != null) {
			pattern = _parent.getTemplate(sName, false);
		}
		if(pattern == null) {
			if(bRaiseError) {
				throw new IllegalStateException("No template '" + sName + "' found!");
			}
			else {
				pattern = new NotMatchedPattern();
			}
		}
		return pattern;
	}
	
	@Override
	public FileMatched matches(File file, boolean bNested) {
		FileMatched matched;
		
		if(!bNested) {
			matched = getTemplate("**pre**", false).matches(file, true);
			if(matched.isMatched() || matched.isStopEvaluation())
				return matched;
		}
		
		for(IFilePattern pattern : _patterns) {
			matched = pattern.matches(file, true);
			if(matched.isMatched() || matched.isStopEvaluation())
				return matched;
		}

		if(!bNested) {
			matched = getTemplate("**post**", false).matches(file, true);
			if(matched.isMatched() || matched.isStopEvaluation())
				return matched;
		}
		
		return _patterns.size() == 0 ? FileMatched.MATCHED : FileMatched.NOT_MATCHED;
	}
	

}
