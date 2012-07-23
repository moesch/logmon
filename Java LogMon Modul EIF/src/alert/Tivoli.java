/*
 * Author: <thomas.weidlich@die-moesch.de>
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

package alert;

import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Logger;

import mon.evt.IAlert;

import com.tivoli.tec.event_delivery.*;

/**
 *
 */
public class Tivoli implements IAlert {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private TECAgent tec;
	private String hostname = null;

	/*
	 * @see mon.evt.IAlert#init(java.util.Properties)
	 */
	@Override
	public boolean init(Properties properties) {
		boolean rc = false;

		// Get local hostname
		try{
			InetAddress localMachine = InetAddress.getLocalHost();
			hostname = localMachine.getHostName();
		} catch(UnknownHostException e){
			logger.fine("Can't get local hostname");
		}

		String tec1 = null;
		String tec2 = null;

		if(properties.containsKey("init.tec1")){
			rc = true;
			tec1 = properties.getProperty("init.tec1");
		}

		if(properties.containsKey("init.tec2")){
			rc = true;
			tec2 = properties.getProperty("init.tec2");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("EventMaxSize=4096\n");
		sb.append("BufEvtPath=send_event.cache\n");

		sb.append("ConnectionMode=connection_oriented\n");

		sb.append("TransportList=t1\n");
		sb.append("t1Type=SOCKET\n");

		sb.append("t1Channels=");
		if(tec1 != null){
			sb.append("c1");
		}

		if(tec1 != null && tec2 != null){
			sb.append(",");
		}

		if(tec2 != null){
			sb.append("c2");
		}
		sb.append("\n");

		if(tec1 != null){
			sb.append("c1ServerLocation=");
			sb.append(tec1);
			sb.append("\n");
			sb.append("c1Port=5529\n");
		}

		if(tec2 != null){
			sb.append("c2ServerLocation=");
			sb.append(tec2);
			sb.append("\n");
			sb.append("c2Port=5529\n");
		}

		String conf = sb.toString();

		logger.fine("Confguration is:\n" + conf);

		Reader reader = new StringReader(conf);

		try{
			tec = new TECAgent(reader, TECAgent.SENDER_MODE, false);

		} catch(EDException e){
			logger.severe("Can't connect to tec");
			rc = false;
		}

		return rc;
	}

	/*
	 * @see mon.evt.IAlert#send(java.util.Properties)
	 */
	@Override
	public boolean send(Properties properties) {
		boolean rc = false;

		logger.fine("Create Tivoli Event with " + properties);

		TECEvent event = new TECEvent();

		event.setClassName(properties.getProperty("send.class"));

		if(properties.getProperty("send.severity").equals("INFO")){
			event.setSlot("severity", "HARMLESS");
		} else{
			event.setSlot("severity", properties.getProperty("send.severity"));
		}
		event.setSlot("msg", '"' + properties.getProperty("send.msg") + '"');

		String repeat = properties.getProperty("send.repeat");
		if(repeat != null){
			event.setSlot("repeat_count", repeat);
		}

		String send_hostname = properties.getProperty("send.hostname");
		if(send_hostname != null){
			event.setSlot("hostname", send_hostname);
		} else if(hostname != null){
			event.setSlot("hostname", hostname);
		}

		if(hostname != null){
			event.setSlot("adapter_host", hostname);
		}

		event.setSlot("source", "LogMon");

		for(String key : properties.stringPropertyNames()){
			String value = properties.getProperty(key);
			logger.fine("Slot property " + key + " : " + value);

			if(key.startsWith("send.slot")){
				String slotname = key.substring(10);
				logger.fine("Set slot " + slotname + " = " + value);
				event.setSlot(slotname, '"' + value + '"');
			}
		}

		int ret = tec.sendEvent(event.toString(true)); // fire!

		if(ret > 0){
			rc = true;
		} else{
			logger.warning("Sendevent returned with code " + ret);
		}

		return rc;
	}

	/*
	 * @see mon.evt.IAlert#stop()
	 */
	@Override
	public boolean stop() {

		tec.disconnect();

		return true;
	}

}
