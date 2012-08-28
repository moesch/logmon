/**
 * Project: Java LogMon
 * Author:  Thomas.weidlich
 *
 * $Id$
 * $Source$
 *
 * Description:
 *
 *
 */
package cfg;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 *
 */
class GlobFilenameFilter implements FilenameFilter {
	private final Pattern glob_regex;

	/**
	 * Test single file.
	 * 
	 * @see java.io.FilenameFilter
	 * 
	 */
	public GlobFilenameFilter(String glob) {
		// Translate glob to regex
		String s1 = glob.replaceAll("\\.", "\\."); // protect (quote) , in regex
		String s2 = s1.replaceAll("\\*", ".*");
		String s3 = s2.replaceAll("\\?", ".");

		glob_regex = Pattern.compile(s3);
	}

	/*
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File dir, String name) {
		return glob_regex.matcher(name).matches();
	}
}