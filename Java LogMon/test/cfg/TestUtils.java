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

}
