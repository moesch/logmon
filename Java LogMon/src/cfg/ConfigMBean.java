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
import java.util.Properties;

import mon.LogSource;

/**
 *
 */
public interface ConfigMBean {

	/**
	 * Get List of LogSource
	 * 
	 * @return All LogSource from configuration file
	 * 
	 * @see LogSource()
	 */
	public abstract List<LogSource> getLogSources();

	/**
	 * Get database id
	 * 
	 * @return the database id from configuration file
	 */
	public abstract String getDBID();

	/**
	 * Get path to database folder
	 * 
	 * @return Path from config file or user's current working directory
	 */
	public abstract String getDBPath();

	/**
	 * Get name of alertclass from configuration
	 * 
	 * @return Name of class
	 */
	public abstract String getAlertClass();

	/**
	 * Get the init properties
	 * 
	 * 
	 * @return The Properties
	 */
	public abstract Properties getAlertInit();

	/**
	 * Get the send properties
	 * 
	 * @return The Properties
	 */
	public abstract Properties getAlertSend();

	/**
	 * Get the stop properties
	 * 
	 * @return The Properties
	 */
	public abstract Properties getAlertStop();

	/**
	 * Get Config File Name
	 */
	public abstract String getConfigFileName();
	
	/**
	 * Get path of PID file or null
	 */
	public abstract String getPIDPath();
}