/*
 * $Id: DataBase.java,v 1.12 2012-07-19 06:23:19 thomas Exp $
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The corralation database to store matched ocurrence. 
 * The db file is a simple csv file. it is only used for
 * restart LogMon to recreate the Map db. 
 * 
 * The real use data are in memory (Map)!!!
 */
public class DataBase implements Runnable, DataBaseMBean {
	private final File db_file;
	private boolean is_dirty = false;

	private final Thread thread = new Thread(this);
	private static int LOOPTIME = 3000;	//ms 

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private ConcurrentHashMap<String, Occurrence> db = new ConcurrentHashMap<String, Occurrence>();

	/**
	 * Create correlation database and load existing data into map
	 * 
	 * @param dbid : id of this database file to distinguish multiple LogMon instances
	 * 
	 */
	public DataBase(String dbid, String path) {
		super();

		String sep = System.getProperty("file.separator");
		if(path.endsWith(sep)){
			String fname = path + "LogMon" + dbid + ".db";
			db_file = new File(fname);
		} else{
			String fname = path + sep + "LogMon" + dbid + ".db";
			db_file = new File(fname);
		}

		logger.fine("DB file " + db_file.getAbsolutePath());

		// Start data save thread
		thread.start();

		// Load data into map
		if(db_file.canRead()){
			logger.fine("Load data from file " + db_file.getAbsolutePath());

			try{
				db = new ConcurrentHashMap<String, Occurrence>();

				BufferedReader br = new BufferedReader(new FileReader(db_file));
				String line;
				while((line = br.readLine()) != null){
					Scanner scanner = new Scanner(line);
					scanner.useDelimiter(";"); // TODO groups contains ; ?

					// Map key
					String key = scanner.next();

					// Map value
					Occurrence o = new Occurrence();
					o.created = scanner.nextLong();
					o.modified = scanner.nextLong();
					o.maxage = scanner.nextLong();
					o.repeat = scanner.nextInt();

					List<String> l = new ArrayList<String>();
					while(scanner.hasNext()){
						l.add(scanner.next());
					}
					String[] a = new String[l.size()];
					o.groups = l.toArray(a);

					scanner.close();

					logger.fine("Add from file " + key + " -> " + o);

					db.put(key, o);
				}

			} catch(Exception e){
				logger.log(Level.SEVERE, "Can't read database", e);
			}
		}
	}

	/**
	 * Stop data save thread
	 */
	public void shutdown() {

		// Wait some time to save open (dirty) data
		try{
			Thread.sleep(2 * LOOPTIME);
		} catch(InterruptedException e){
		}

		// stop thread
		thread.interrupt();
	}

	/**
	 * Store object in db
	 * 
	 * @param key : String to identify object in database
	 * @param data : Object to save
	 * 
	 * @return the updated version (repeat, modified)
	 */
	synchronized public Occurrence save(String key, Occurrence data) {
		Occurrence o = data;

		logger.fine("Save " + key);
		if(db.containsKey(key)){
			logger.fine("Enhance repeat and update modified");

			o = db.get(key);
			data.repeat = o.repeat + 1;
			data.modified = new Date().getTime();
		}

		logger.fine("Update object in db: " + key + " -> " + data);

		db.put(key, data);
		setDirty(true);

		return o;
	}

	/**
	 * Load Object from database
	 * 
	 * @param key : String to identify object in database
	 * 
	 * @return The object or NULL
	 * 
	 */
	synchronized public Occurrence load(String key) {

		if(db.containsKey(key)){
			logger.fine("Get object from db: " + key);
			return db.get(key);
		}

		return null;
	}

	/*
	 * @see mon.DataBaseMBean#remove(java.lang.String)
	 */
	@Override
	synchronized public void remove(String key) {
		if(db.containsKey(key)){
			logger.fine("Remove object from db: " + key);
			db.remove(key);
			setDirty(true);
		}
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.fine("Start data save thread");

		// Write data to file
		while(!thread.isInterrupted()){

			long now = new Date().getTime();

			for(String key : db.keySet()){
				Occurrence o = db.get(key);

				if(o.maxage != 0 && now > o.modified + o.maxage * 1000L){
					logger.fine("Garbage collector remove db entry: " + key);
					logger.fine("Now: " + new Date(now).toString());
					logger.fine("Modified: " + new Date(o.modified).toString() + " Maxage: " + o.maxage);
					logger.fine("Max date: " + new Date(o.modified + o.maxage * 1000L).toString());

					remove(key);
				}
			}

			if(is_dirty){
				logger.fine("Write data to file " + db_file.getAbsolutePath());
				logger.fine("Database entry count " + db.size());

				try{
					PrintWriter pw = new PrintWriter(db_file);

					for(String key : db.keySet()){
						logger.fine("Write key " + key);

						Occurrence o = db.get(key);
						pw.printf("%s;%d;%d;%d;%d", key, o.created, o.modified, o.maxage, o.repeat);

						for(String group : o.groups){
							pw.printf(";%s", group);
						}
						pw.append("\n");
					}
					pw.close();

					setDirty(false);

				} catch(Exception e){
					logger.log(Level.SEVERE, "Database IO Error", e);
				}
			}

			try{
				Thread.sleep(LOOPTIME);
			} catch(InterruptedException e){
			}
		}

		logger.fine("Data save thread stopped");
	}

	/*
	 * @see mon.DataBaseMBean#setDirty(boolean)
	 */
	@Override
	synchronized public boolean setDirty(boolean flag) {
		boolean last = is_dirty;
		is_dirty = flag;
		return last;
	}

	/*
	 * @see mon.DataBaseMBean#isDirty()
	 */
	@Override
	synchronized public boolean isDirty() {
		return is_dirty;
	}

	/*
	 * @see mon.DataBaseMBean#getSize()
	 */
	@Override
	public int getSize() {
		return db.size();
	}

	/*
	 * @see mon.DataBaseMBean#getKeys()
	 */
	@Override
	public String[] getKeys() {
		String[] a = new String[0];
		return db.keySet().toArray(a);
	}

}
