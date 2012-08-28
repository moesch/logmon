/*
 * Author: <thomas@die-moesch.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor Boston, MA 02110-1301,  USA
 */

package cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Util {

	/**
	 * Resolve $ENV{}
	 * 
	 * 
	 * @param in : String with $ENV{} entry's
	 * 
	 * @return String with resolved environment variables
	 */

	static public String resolvENV(String in) {

		int pos = 0;
		StringBuilder sb = new StringBuilder();

		Pattern env_pattern = Pattern.compile("\\$ENV\\{([\\w\\d]+)\\}");
		Matcher env_matcher = env_pattern.matcher(in);
		while(env_matcher.find()){
			String var = env_matcher.group(1);
			String value = System.getenv(var);

			// Add static part from current pos to placeholder
			sb.append(in.substring(pos, env_matcher.start()));
			pos = env_matcher.end();

			// Substitute placeholder
			sb.append(value);

		}
		sb.append(in.substring(pos, in.length()));

		return sb.toString();
	}

	/**
	 * Resolve Shell Glob. (Don't dive into folder)
	 * 
	 * <table>
	 * <tr>
	 * <td>Match one unknown character</td>
	 * <td>?</td>
	 * </tr>
	 * <tr>
	 * <td>Match any number of unknown characters</td>
	 * <td>*</td>
	 * </tr>
	 * </table>
	 * 
	 * @param glob : String with shell glob
	 * @param folder : The start directory folder or NULL
	 * 
	 * @return List of resolved filenames
	 */
	static public List<String> resolvGlob(String glob, String folder) {
		String current_folder = folder;
		String file_sep = System.getProperty("file.separator");

		List<String> result_list = new ArrayList<String>();

		// Use current folder if no folder given
		if(current_folder == null){
			current_folder = System.getProperty("user.dir");
		}

		// No real folder given
		if(!new File(current_folder).isDirectory()){
			return null;
		}

		// Split glob into single folder part
		String[] tree = glob.split("[\\/]");

		// Parse tree parts
		StringBuffer pathbilder = new StringBuffer(current_folder);
		for(int idx = 0; idx < tree.length; idx++){

			String tree_part = tree[idx];

			if(!(tree_part.contains("*") || tree_part.contains("?"))){
				// no glob in part
				pathbilder.append(file_sep).append(tree_part);

				current_folder = pathbilder.toString();
			} else{
				// resolv glob's
				File dir = new File(current_folder);
				if(dir.isDirectory()){
					GlobFilenameFilter filter = new GlobFilenameFilter(tree_part);

					String[] filenames = dir.list(filter);
					for(String filename : filenames){

						File f = new File(current_folder + file_sep + filename);
						if(f.isFile()){
							result_list.add(f.getAbsolutePath());
						} else{

							StringBuffer rest_glob = new StringBuffer();
							for(int idy = idx + 1; idy < tree.length; idy++){
								rest_glob.append(tree[idy]);
								if(idy + 1 < tree.length){
									rest_glob.append(file_sep);
								}
							}
							List<String> sub_list = resolvGlob(rest_glob.toString(), f.getAbsolutePath());
							result_list.addAll(sub_list);

						}
					}
					idx++;

				}
			}
		}

		return result_list;
	}
}
