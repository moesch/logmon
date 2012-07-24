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

import java.io.File;
import java.util.logging.*;

import org.junit.*;

/**
 *
 */
public class TestDataBase {
	DataBase db = null;
	static Logger logger;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger = Logger.getLogger("");
		logger.setLevel(Level.ALL);

		logger.setUseParentHandlers(false);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		logger.addHandler(ch);

		File f = new File("LogMonjunit.db");
		if(f.exists()){
			f.delete();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		db = new DataBase("junit", System.getProperty("java.io.tmpdir"));

	}

	@After
	public void shutdown() {
		db.shutdown();
	}

	/**
	 * Test method for {@link mon.DataBase#save(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public final void testSave() {
		Occurrence o = new Occurrence();
		o.maxage = 1 * 3600;
		String[] g1 = { "Koelle", "alaaf" };
		o.groups = g1;
		Occurrence n = db.save("test1", o);
		Assert.assertTrue(n.repeat == 0);

		o = new Occurrence();
		o.maxage = 2 * 3600;
		String[] g2 = { "Moin", "Moin" };
		o.groups = g2;
		n = db.save("test2", o);
		n = db.save("test2", o);
		n = db.save("test2", o);

		Assert.assertTrue(n.repeat == 2);

		o = new Occurrence();
		o.maxage = 3 * 3600;
		String[] g3 = { "Zicke", "Zacke" };
		o.groups = g3;
		n = db.save("test3", o);
		Assert.assertTrue(n.repeat == 0);
		Assert.assertTrue(n.maxage == 3 * 3600);
	}

	/**
	 * Test method for {@link mon.DataBase#load(java.lang.String)}.
	 */
	@Test
	public final void testLoad() {
		Occurrence o = db.load("test1");

		Assert.assertTrue(o.maxage == 3600);
		Assert.assertTrue(o.groups[0].equals("Koelle"));
	}

	/**
	 * Test remove
	 */
	@Test
	public final void testRemove() {
		db.remove("test1");
		Occurrence o = db.load("test1");
		Assert.assertTrue(o == null);
	}

	@Test
	public final void testGC() {

		Occurrence o1 = new Occurrence();
		o1.maxage = 2;
		db.save("gctest1", o1);

		try{
			Thread.sleep(3000);
		} catch(InterruptedException e){
		}

		Occurrence o2 = db.load("gctest1");
		Assert.assertTrue(o2 == null);

		Occurrence o3 = new Occurrence();
		o3.maxage = 5;
		db.save("gctest2", o3);

		try{
			Thread.sleep(2000);
		} catch(InterruptedException e){
		}

		Occurrence o4 = db.load("gctest2");
		Assert.assertTrue(o4 != null);

	}
}
