/*
 * $Id: StartPosition.java,v 1.2 2012-07-05 20:45:55 thomas Exp $
 *
 * Author: <thomas.weidlich@die-moesch.de>
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

/**
 * Possible start positions while open logfile
 *
 * <ul>
 * 	<li>CURRENT : The crurent logfile position</li>
 *  <li>BEGIN : Read from begin of logfile
 * </ul>
 *
 * @author thomas.weidlich@die-moesch.de
 */
public enum StartPosition {
	BEGIN,CURRENT,LAST
}
