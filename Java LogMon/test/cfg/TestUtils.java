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

import java.util.List;

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
		List<String> result;

		result = Util.resolvGlob("doc/i*/*", null);
		Assert.assertTrue(result != null && result.size() > 0);

		result = Util.resolvGlob("doc/*.tex", null);
		Assert.assertTrue(result != null && result.size() == 1);

		result = Util.resolvGlob("src/*/*.java", null);
		Assert.assertTrue(result != null && result.size() > 0);

		result = Util.resolvGlob("*.java", null);
		Assert.assertTrue(result != null && result.size() == 0);

		result = Util.resolvGlob("*", "abcdefghij-folder-not-exists");
		Assert.assertNull(result);
	}
}
