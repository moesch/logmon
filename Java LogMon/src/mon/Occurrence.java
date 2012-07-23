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

package mon;

import java.io.Serializable;
import java.util.Date;

/**
 * Instance of matched or store ocurrence
 * 
 * All properties are public because of JavaScript using
 * 
 */
public class Occurrence implements Serializable {
	private static final long serialVersionUID = 1L;

	public long created = 0l;
	public long modified = 0l;
	public long maxage = 0l;
	public int repeat = 0;
	public String[] groups;

	public Occurrence() {
		super();

		// Init defaults

		// created and modified are same time
		created = new Date().getTime();
		modified = created;

		// 7Days
		maxage = 7 * 24 * 3600L;

		// no repeat
		repeat = 0;

		groups = new String[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Occurrence C:%d M:%d MX:%d R:%d G:%d", created, modified, maxage, repeat, groups.length);
	}

}
