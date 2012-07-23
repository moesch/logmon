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

import java.util.ArrayList;
import java.util.List;

import cfg.StartPosition;

/**
 * A source instance contains the logsource and the pattern
 */
public class LogSource {
	private String filename;
	private List<LogPattern> patternlist = new ArrayList<LogPattern>();
	private StartPosition sp = StartPosition.BEGIN;

	/**
	 * Create new instance
	 */
	public LogSource() {
	}

	/**
	 * Create new instance with given filename
	 */
	public LogSource(String filename) {
		this.filename = filename;
	}

	/**
	 * Create new instance with given filename and pattern list
	 */
	public LogSource(String filename, List<LogPattern> pattern) {
		this.filename = filename;
		this.patternlist = pattern;
	}

	/**
	 * Set filename
	 *
	 * @param filename The filename to set. contain placesholders resolved while open file
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Get filename with unresolved placeholders
	 *
	 * @return filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * Add pattern to list
	 *
	 * @param logpattern The logpattern to add
	 * @see LogPattern()
	 */
	public void addPattern(LogPattern logpattern) {
		patternlist.add(logpattern);
	}

	/**
	 * Get list of LogPattern
	 *
	 * @return List of LogPattern
	 * @see LogPattern()
	 */
	public List<LogPattern> getPatternList() {
		return patternlist;
	}

	/**
	 * Set the StartPosition
	 *
	 * @param sp : The StartPosition
	 *
	 * @see StartPosition();
	 */
	public void setStartPosition(StartPosition sp) {
		this.sp=sp;

	}

	/**
	 * Get the StartPosition
	 *
	 * @return StartPosition
	 */
	public StartPosition getStartPosition() {
		return sp;
	}


}
