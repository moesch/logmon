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

/**
 *
 */
public interface WatchSourceMBean {

	/**
	 * Get logfile name
	 * 
	 * @return The filename as string
	 */
	public abstract String getFilename();

	/**
	 * Get last reading position
	 * 
	 * @return The current reading position as long value
	 */
	public abstract long getReadingPosition();

	/**
	 * Set new reading position of file. Position 0 is start of file
	 * 
	 * @param pos : A long value of new position
	 */
	public abstract void setReadingPosition(long pos);

}
