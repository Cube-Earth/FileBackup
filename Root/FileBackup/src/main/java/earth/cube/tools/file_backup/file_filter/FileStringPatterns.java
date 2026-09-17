package earth.cube.tools.file_backup.file_filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import earth.cube.tools.file_backup.commons.FileUtil;

/** Examples:
 * 
 * ** /*.java
 * -test.java
 * ** /*.txt
 */
public class FileStringPatterns implements IFilePattern {
	
	private File _baseDir;
	private List<String> _patterns = new ArrayList<>();

	public FileStringPatterns(File baseDir) {
		_baseDir = baseDir;
	}

	public void add(String sPattern) {
		_patterns.add(sPattern);
	}

	protected boolean matches(String sRelPath, String sPattern) {
		String sRegExp;
		if(sPattern.startsWith(":")) {
			sRegExp = sPattern.substring(1);
		}
		else {
			sRegExp = sPattern.replaceAll("([?.[{}\\]+])", "\\1").replaceAll("*", "[^/]+").replaceAll("**", ".*");
			if(sPattern.startsWith("/"))    // in case pattern should match only for first level, write "/*.java" instead of "*.java"
				sPattern = sPattern.substring(1);
			else
				if(sPattern.indexOf('/') == -1)
					sRegExp = "(.+/)?" + sRegExp;
		}
		return sRelPath == null ? false : sRelPath.matches(sRegExp);
	}
	
	@Override
	public FileMatched matches(File file, boolean bNested) {
		String sRelPath = FileUtil.getRelativePath(_baseDir, file);
		if(sRelPath == null)
			return FileMatched.NOT_MATCHED;
		
		int nPatternIdx = -1;
		
		boolean bRoot;
		boolean bInclude;
		boolean bMatchesExpr;
		boolean bStopEvaluation;

		boolean bMatchesFilter = true;
		for(String sPattern : _patterns) {
			nPatternIdx++;
			if(sPattern.charAt(0) == '!') {
				bStopEvaluation = true;
				sPattern = sPattern.substring(1);
			}
			else {
				bStopEvaluation = false;
			}
			switch(sPattern.charAt(0)) {
				case '+':
					bRoot = false;
					bInclude = true;
					sPattern = sPattern.substring(1);
					break;
					
				case '-':
					bRoot = false;
					bInclude = false;
					sPattern = sPattern.substring(1);
					break;
					
				default:
					bRoot = true;
					bInclude = true;
					break;
			}
			
			bMatchesExpr = matches(sRelPath, sPattern);
			
			if(nPatternIdx > 0 && bMatchesFilter && bRoot)
				return FileMatched.MATCHED;
			
			if(bRoot)
				bMatchesFilter = true;
			
			bMatchesFilter &= (bInclude && bMatchesExpr) || (!bInclude && !bMatchesExpr);
			if(bMatchesExpr && bStopEvaluation)
				return new FileMatched(bMatchesFilter, true);
		}
		return new FileMatched(bMatchesFilter, false);
	}

}
