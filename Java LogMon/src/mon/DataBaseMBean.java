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
public interface DataBaseMBean {

	/**
	 * Remove object from db
	 * 
	 * @param key : String to identify object in database
	 * 
	 */
	public abstract void remove(String key);

	/**
	 * Change database dirty flag to dump memory in file Thread save synchronised version
	 * 
	 * @param flag The new states
	 * 
	 * @return The old state
	 */
	public abstract boolean setDirty(boolean flag);

	/**
	 * Get dirty state of database. The database is dirty when data change in memory but not yet written to filesystem
	 * 
	 * @return true/false
	 * 
	 */
	public abstract boolean isDirty();

	/**
	 * Get count of database entry
	 * 
	 * @return count of entrys
	 */
	public abstract int getSize();

	/**
	 * Get array of db keys
	 * 
	 * @return A string array with keys
	 * 
	 */
	public String[] getKeys();
}