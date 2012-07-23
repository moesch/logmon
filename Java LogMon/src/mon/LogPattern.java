/*
 * $Id: LogPattern.java,v 1.18 2012-07-13 10:27:28 thomas Exp $
 *
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

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

import javax.script.*;

import mon.evt.Severity;
import app.LogMon;
import cfg.Util;

/**
 *
 */
public class LogPattern {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private Pattern pattern;

	private String msg;
	private String[] groups;
	private Severity severity = Severity.WARNING;

	private String condition_filename = null;
	private final ScriptEngine jsengine;

	private Occurrence occurrence = new Occurrence();

	private Properties send_properties = new Properties();

	/**
	 * Create instance
	 */
	public LogPattern() {
		ScriptEngineManager factory = new ScriptEngineManager();
		jsengine = factory.getEngineByName("JavaScript");
	}

	/**
	 * Set new pattern
	 * 
	 * @param regex The regex to set
	 */
	public void setPattern(String regex) {
		try{
			this.pattern = Pattern.compile(regex);

		} catch(PatternSyntaxException e){
			logger.warning("Syntax error in pattern: " + regex);
			this.pattern = null;
		}
	}

	/**
	 * Set new message. The string maybe contain placeholders. The placeholders will resolved while sending/using time
	 * 
	 * @param msg : The message to set
	 */
	public void setMessage(String msg) {
		this.msg = msg;
	}

	/**
	 * Get the message.
	 * 
	 * Resolve $1,$2,$3 ... to Regex groups of last match . Resolve $ENV{VARNAME} to environment variable VARNAME
	 * 
	 * @return String resolved message
	 */
	public String getMessage() {
		return resolvString(msg);
	}

	/**
	 * Resolv enviromment an groups
	 * 
	 * @return resovled string
	 */
	private String resolvString(String in) {
		StringBuilder sb = new StringBuilder();
		int pos = 0;

		// Group from user pattern
		Pattern group_pattern = Pattern.compile("\\$(\\d+)");
		Matcher group_matcher = group_pattern.matcher(in);
		while(group_matcher.find()){
			int idx = Integer.parseInt(group_matcher.group(1));

			if(idx <= groups.length){
				// Add static part from current pos to placeholder
				sb.append(in.substring(pos, group_matcher.start()));
				pos = group_matcher.end();

				// Substitute placeholder
				sb.append(groups[idx - 1]);
			} else{
				logger.warning("Wrong index $" + idx + " in message " + in);
			}
		}
		sb.append(in.substring(pos, in.length()));

		return Util.resolvENV(sb.toString());
	}

	/**
	 * Set the severity
	 * 
	 * @param severity to set
	 */
	public void setSeverity(String severity) {
		try{
			this.severity = Severity.valueOf(severity.toUpperCase());
		} catch(Exception e){
			logger.warning("Wrong severity given: " + severity);
		}
	}

	/**
	 * Get the severity
	 * 
	 * @return The severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * Set the condition script filename.
	 * 
	 * @param filename
	 */
	public void setConditionFile(String filename) {

		String fname = Util.resolvENV(filename);

		if(new File(fname).canRead()){
			condition_filename = fname;
		}
	}

	/**
	 * Check line from logfile against this pattern and update regex groups if matches invoke condition script
	 * 
	 * @param line The line to test again pattern
	 * @see #getMessage()
	 * 
	 * @return true if match
	 */
	public boolean matches(String line) {
		boolean check_success = false;

		if(pattern == null){
			return false;
		}

		// Check pattern
		Matcher matcher = pattern.matcher(line);

		int cnt = matcher.groupCount();
		groups = new String[cnt];
		while(matcher.find()){
			logger.fine("Matched: " + line);
			check_success = true;
			for(int idx = 0; idx < cnt; idx++){
				groups[idx] = matcher.group(idx + 1);
			}
		}

		// Check condition if pattern matched
		if(check_success && condition_filename != null){
			try{
				Reader reader = new FileReader(condition_filename);

				String status = "false";

				occurrence = new Occurrence();
				occurrence.groups = this.groups;

				DBConnect dbc = new DBConnect();

				// give vars to script
				jsengine.put("db", dbc);
				jsengine.put("occurrence", occurrence);

				jsengine.put("pattern", pattern);
				jsengine.put("logline", line);
				jsengine.put("status", status);
				jsengine.put("msg", msg);

				// execute
				jsengine.eval(reader);

				// Get changed variables from script
				status = jsengine.get("status").toString();
				msg = jsengine.get("msg").toString();

				// Only allowed values
				Occurrence tempocurr = (Occurrence) jsengine.get("occurrence");
				logger.fine("Get from JS " + occurrence);

				occurrence.maxage = tempocurr.maxage;
				occurrence.groups = tempocurr.groups;
				occurrence.modified = tempocurr.modified;

				logger.fine("Condition check result is " + status);
				if(!status.equalsIgnoreCase("true")){
					check_success = false;
				}

			} catch(FileNotFoundException e){
				logger.warning("Condition file not found " + condition_filename);

			} catch(ScriptException e){
				logger.log(Level.WARNING, "Condition script exception on file " + condition_filename + " Message: " + e.getMessage());
			}
		}

		return check_success;
	}

	/*
	 * Helper class for give some methode from DataBase to JavaScript
	 */
	class DBConnect {
		DataBase database = LogMon.getInstance().getDatabase();

		public void save(String key) {
			database.save(key, occurrence);
		}

		public Occurrence load(String key) {
			return database.load(key);
		}
	}

	/**
	 * Set pattern related sendproperties
	 * 
	 * @param properties
	 */
	public void setSendProperties(Properties properties) {
		send_properties = properties;

	}

	/**
	 * Get resolved send properties
	 * 
	 * @return Resolved properties
	 * 
	 */
	public Properties getSendProperties() {

		// Resolv
		Properties p = new Properties();
		for(String key : send_properties.stringPropertyNames()){

			if(key.startsWith("send")){
				String value = send_properties.getProperty(key);

				logger.fine("Resolve " + key + " = " + value);
				// Resolv ENV
				value = Util.resolvENV(value);

				// Resolv groups
				value = resolvString(value);

				// add
				logger.fine("Resolved " + key + " = " + value);
				p.setProperty(key, value);
			}
		}

		return p;
	}

	/**
	 * Get occurrence
	 * 
	 * @return occurrence of last match or null
	 */
	public Occurrence getOccurrence() {
		return occurrence;
	}
}
