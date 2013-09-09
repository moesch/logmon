/*
 * Author: <thomas@die-moesch.de>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor Boston, MA
 * 02110-1301, USA
 */

package mon;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

import mon.evt.IAlert;
import cfg.*;

/**
 * Monitore one LogSource
 */
public class WatchSource implements Runnable, WatchSourceMBean {
	private final Logger	logger		= Logger.getLogger(this.getClass().getName());

	private LogSource		logsource	= null;
	private IAlert			alert		= null;
	private Config			config		= null;

	// Last reading position
	private long			last_pos	= 0;

	// Some static configurations
	private static long		NORMALSLEEP	= 1000l;
	private static int		MAXTRY		= 200;

	/**
	 * Monitor one LogSource
	 * 
	 * @param alert
	 * @param config
	 * 
	 * @param The
	 *            LogSource to watch
	 * @see LogSource();
	 * 
	 */
	public WatchSource(LogSource logsource, IAlert alert, Config config) {
		this.logsource = logsource;
		logger.fine("Create Instance of " + logsource.getFilename());

		this.alert = alert;
		this.config = config;
	}

	/*
	 * @see mon.WatchSourceMBean#getFilename()
	 */
	@Override
	public String getFilename() {
		return logsource.getFilename();
	}

	/*
	 * @see mon.WatchSourceMBean#getReadingPosition()
	 */
	@Override
	public long getReadingPosition() {
		return last_pos;
	}

	/*
	 * @see mon.WatchSourceMBean#setReadingPosition(long)
	 */
	@Override
	public void setReadingPosition(long pos) {
		this.last_pos = pos;
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		if (logsource == null) {
			logger.warning("No LogSource given");
			return;
		}

		String filename = logsource.getFilename();
		File file = new File(Util.resolvENV(filename));

		String sep = System.getProperty("file.separator");
		String folder = config.getDBPath();

		File last_pos_file;

		if (folder.endsWith(sep)) {
			last_pos_file = new File(folder + "LastPos_" + logsource.getId() + "_" + file.getName() + ".db");
		} else {
			last_pos_file = new File(folder + sep + "LastPos_" + logsource.getId() + "_" + file.getName() + ".db");
		}
		logger.fine("Last positions file " + last_pos_file.getAbsolutePath());

		StartPosition startposition = logsource.getStartPosition();
		switch (startposition) {
		case BEGIN:
			last_pos = 0L;
			break;

		case CURRENT:
			last_pos = file.length();
			break;

		case LAST:
			if (last_pos_file.canRead()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(last_pos_file));
					last_pos = Long.parseLong(br.readLine());
					br.close();

					logger.fine("Last position of " + file.getName() + " restored from file to " + last_pos);

				} catch (Exception e) {
					logger.warning("Can't read lat position from file. Use current position " + e.getMessage());
					last_pos = file.length();
				}
			}
			break;

		default:
			last_pos = 0L;
		}
		logger.fine("StartPosition " + startposition + " set position to " + last_pos + " of " + file.getName());

		long sleep = NORMALSLEEP;

		// Emergency exit
		boolean watchit = true;
		long maxtry = MAXTRY;

		while (watchit) {
			long len = file.length(); // get current length

			if (!file.canRead()) {
				logger.info("Can't read logfile " + file.getAbsolutePath() + " sleep deeper. SleepSec: " + sleep / 1000);
				if (sleep < 5 * 60 * 1000L) {
					sleep *= 2;
				}

				// Emergency exit
				if (--maxtry <= 0) {
					watchit = false;
					logger.warning("Can't read file! Give up " + file.getAbsolutePath());
				}

			} else {
				maxtry = MAXTRY;
				sleep = NORMALSLEEP;
			}

			// Read new lines if file length changed
			if (len != last_pos) {

				try {
					RandomAccessFile raf = new RandomAccessFile(file, "r");
					if (len > last_pos) {
						// If logfile grown, set read position to last read position
						raf.seek(last_pos);
					} else {
						// LogSource truncated.
						switch (startposition) {
						case BEGIN:
							last_pos = 0L;
							break;

						case CURRENT:
							last_pos = file.length();
							break;

						default:
							last_pos = 0L;
						}

						raf.seek(last_pos);
					}

					Properties modul_properties = config.getAlertSend();

					String line = null;
					while ((line = raf.readLine()) != null) {
						for (LogPattern logpattern : logsource.getPatternList()) {
							if (logpattern.matches(line)) {

								Occurrence o = logpattern.getOccurrence();

								Properties p = new Properties();
								p.setProperty("send.logline", line);
								p.setProperty("send.msg", logpattern.getMessage());
								p.setProperty("send.severity", logpattern.getSeverity().name());
								p.setProperty("send.repeat", Integer.toString(o.repeat));

								// Add modul properties
								for (String key : modul_properties.stringPropertyNames()) {
									p.setProperty(key, modul_properties.getProperty(key));
								}

								// Add logpattern properties
								Properties pattern_properties = logpattern.getSendProperties();
								for (String key : pattern_properties.stringPropertyNames()) {
									p.setProperty(key, pattern_properties.getProperty(key));
								}
								logger.fine("Call send with " + p);
								alert.send(p);
							}
						}
					}
					last_pos = raf.getFilePointer();
					raf.close();

					if (last_pos_file.canWrite() || !last_pos_file.exists()) {
						try {
							PrintWriter pr = new PrintWriter(last_pos_file);
							pr.print(last_pos);
							pr.close();
						} catch (Exception ex) {
							logger.warning("Can't write last position to file " + ex.getMessage());
						}
					}

				} catch (FileNotFoundException e) {
					logger.warning("LogSource not found: " + file.getAbsolutePath());
					logger.warning("Stop reading from file: " + file.getAbsolutePath());
					return;

				} catch (IOException e) {
					logger.warning("IOException while reading: " + file.getAbsolutePath());
					logger.warning("Stop reading from file: " + file.getAbsolutePath());
					return;
				}
			}

			// LogSource not changed. Sleep!
			if (len == last_pos) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
