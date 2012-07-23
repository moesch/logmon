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

package mon.evt;

import java.util.Properties;

/**
 * Well known properties
 * 
 * <ul>
 * <li>send.logline</li>
 * <li>send.msg</li>
 * <li>send.severity</li>
 * <li>send.repeat</li>
 * </ul>
 * 
 */
public interface IAlert {

	/**
	 * Initialise alert module. Ex. open files
	 * 
	 * @param properties : Properties to initialise or null
	 * 
	 * @return true on success
	 */
	public boolean init(Properties properties);

	/**
	 * Send Alert to receiver
	 * 
	 * @param properties
	 * 
	 * @return true on success
	 */
	public boolean send(Properties properties);

	/**
	 * Stop alert modul
	 * 
	 * @return true on success
	 */
	public boolean stop();
}
