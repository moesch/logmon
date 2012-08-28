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

import java.util.*;

import junit.framework.Assert;

import org.junit.Test;

public class TestUtils {

	@Test
	public void testResolvENV() {

		String in = "/pre/$ENV{USERNAME}/post";

		String out = Util.resolvENV(in);

		System.out.println("Resolved string is: " + out);
		String usern = System.getenv("USERNAME");

		String ok = "/pre/" + usern + "/post";

		Assert.assertTrue(out.equals(ok));

	}

	@Test
	public void testResolvGlob() {

		@SuppressWarnings("serial")
		Map<String, Integer> all_globs = new HashMap<String, Integer>() {
			{
				put("doc/*.tex", 1);
				put("src/*/*.java", 15);
				put("doc/i*/*.*", 3);
				put("a/b/*", 0);
			}
		};

		System.out.println("-------------------------------------------------------------");
		System.out.println("TEST: testResolvGlob()");

		List<String> result;

		for(String glob : all_globs.keySet()){
			result = Util.resolvGlob(glob, null);

			System.out.println("\nGlob: " + glob + " =>");
			printList(result);
			System.out.println("Count: " + result.size());

			Assert.assertTrue(result != null && result.size() == all_globs.get(glob));

		}
	}

	void printList(List<String> result) {
		for(String s : result){
			System.out.println(s);
		}
	}
}
