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

package app;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import mon.*;
import mon.evt.IAlert;
import cfg.Config;

/**
 *
 *
 */
public class LogMon implements Runnable {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private static LogMon instance = null;
	private static String[] args = null;

	private DataBase db = null;

	private final Config config;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	/**
	 *
	 */
	private LogMon() {

		// Read given configuration file
		config = new Config(args);
		registerMBean(config, "LogMon:type=Config");

		// Write pid to file
		try{
			String pidpath = config.getPIDPath();

			if(pidpath != null){
				File pidfile = new File(pidpath);

				if(pidfile.exists()){
					logger.severe("PID file exists. Remove first!");
					System.exit(10);
				} else{

					PrintWriter pr = new PrintWriter(pidfile);
					pr.print(getPID());
					pr.close();

					pidfile.deleteOnExit();
				}
			}
		} catch(Exception e){
		}

		// ShutdownHook
		Runtime.getRuntime().addShutdownHook(new Thread(this));

		// Read class name from config
		logger.fine("Load alert class");

		IAlert alert = null;
		String classname = "unknown";
		try{
			classname = config.getAlertClass();

			Class<?> clazz = Class.forName(classname);
			alert = (IAlert) clazz.newInstance();

			alert.init(config.getAlertInit());

		} catch(Exception e){
			logger.log(Level.SEVERE, "Class not found: " + classname, e);
			System.exit(20);
		}

		// Monitor all logfiles
		logger.fine("Start monioring logsources");

		for(LogSource logsource : config.getLogSources()){
			WatchSource ws = new WatchSource(logsource, alert, config);

			this.registerMBean(ws, "LogMon:type=WatchSource,name=" + logsource.getFilename());

			threadPool.execute(ws);
		}
		logger.fine("All monitor started");
	}

	/*
	 * ShutdownHook
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		if(db != null){
			db.shutdown();
		}

		threadPool.shutdown();
		logger.fine("All threads stopped");
	}

	/**
	 * Get config
	 *
	 * @return Instance of Config()
	 * @see Config()
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Get instance of Database
	 *
	 */
	public DataBase getDatabase() {

		if(db == null){
			db = new DataBase(config.getDBID(), config.getDBPath());
			registerMBean(db, "LogMon:type=DataBase");
		}

		return db;
	}

	/**
	 * Register MBean
	 */
	public void registerMBean(Object o, String s) {

		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try{
			ObjectName name = new ObjectName(s);
			server.registerMBean(o, name);

		} catch(Exception e){
			logger.warning("Can't register MBean: " + s);
		}
	}

	/**
	 * Get Program arguments
	 *
	 * @return String array argv[]
	 */
	public String[] getArguments() {
		return args;
	}

	/**
	 *
	 */
	private String getPID() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return (name.split("@"))[0];
	}

	/**
	 * Get the one and only
	 *
	 * @return LogMon
	 */
	public static LogMon getInstance() {
		if(instance == null){
			instance = new LogMon();
		}
		return instance;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LogMon.args = args;
		LogMon.getInstance();
	}

}
