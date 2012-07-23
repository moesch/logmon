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

package mon.evt;

import java.util.Properties;

/**
 *
 */
public class StdoutAlert implements IAlert {

	/*
	 * @see mon.evt.IAlert#init()
	 */
	@Override
	public boolean init(Properties properties) {

		System.out.println(properties.get("init.msg"));

		return true;
	}

	/*
	 * @see mon.evt.IAlert#send(java.util.Properties)
	 */
	@Override
	public boolean send(Properties properties) {

		System.out.printf("%s Severity: %s  Message: %s\n", properties.get("send.pre"), properties.get("send.severity"), properties.get("send.msg"));
		for(Object key : properties.keySet()){
			System.out.printf("Send properties  Key: %s  -> Value: %s\n", key, properties.get(key));
		}

		return true;
	}

	/*
	 * @see mon.evt.IAlert#stop()
	 */
	@Override
	public boolean stop() {
		return true;
	}

}
