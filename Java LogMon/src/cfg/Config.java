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

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

import mon.LogPattern;
import mon.LogSource;

import org.w3c.dom.*;

/**
 * Read and parse the config.xml
 */
public class Config implements ConfigMBean {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final List<LogSource> logsources = new ArrayList<LogSource>();

	private String db_id = "Default";
	private String db_path = null;
	private String pid_path = null;

	private String alertclass = "unknown";
	private final Properties alert_init = new Properties();
	private final Properties alert_send = new Properties();
	private final Properties alert_stop = new Properties();

	private File config_file = null;

	/**
	 * Create instance and read configuration file
	 */
	public Config(String[] args) {
		boolean config_found = false;
		for(String arg : args){
			if(arg.startsWith("--config")){
				config_found = true;
				String fname = arg.split("[=:]")[1];
				read(fname);
			}
		}

		if(!config_found){
			logger.warning("No configuration file given. Please use commandline argument --config=file.xml");
		}
	}

	/*
	 * Read config file and add to List<LogSource> logsources
	 */
	private void read(String fname) {
		config_file = new File(fname);

		logger.fine("Read " + config_file.getAbsolutePath());

		DocumentBuilderFactory builderfactory = DocumentBuilderFactory.newInstance();
		builderfactory.setNamespaceAware(true);
		builderfactory.setValidating(true);

		XPathFactory xpathfactory = XPathFactory.newInstance();
		XPath xpath = xpathfactory.newXPath();

		try{
			DocumentBuilder builder = builderfactory.newDocumentBuilder();
			builder.setEntityResolver(new ConfigEnityResolver()); // Validate xml
			builder.setErrorHandler(new ConfigErrorHandler());

			Document document = builder.parse(config_file);

			// database
			Node dbid_nodes = (Node) xpath.evaluate("//config/database/id", document, XPathConstants.NODE);
			db_id = dbid_nodes.getTextContent();

			Node dbpath_nodes = (Node) xpath.evaluate("//config/database/path", document, XPathConstants.NODE);
			if(dbpath_nodes != null){
				db_path = dbpath_nodes.getTextContent();
			} else{
				logger.fine("No path entry of dabatabe found. Use current folder");
				db_path = System.getProperty("user.dir");
			}

			Node pid_nodes = (Node) xpath.evaluate("//config/database/pid", document, XPathConstants.NODE);
			if(pid_nodes != null){
				pid_path = pid_nodes.getTextContent();
			}

			// IAlert
			Node alert_nodes = (Node) xpath.evaluate("//config/alert/class", document, XPathConstants.NODE);
			alertclass = alert_nodes.getTextContent();
			NodeList prop_nodes = (NodeList) xpath.evaluate("//config/alert/properties/property", document, XPathConstants.NODESET);
			for(int idx = 0; idx < prop_nodes.getLength(); idx++){
				Node prop_node = prop_nodes.item(idx);
				Node name_node = prop_node.getAttributes().getNamedItem("name");

				String name = name_node.getTextContent();
				String value = prop_node.getTextContent();

				logger.fine("Get alert property " + name + "=" + value);

				if(name.startsWith("init")){
					alert_init.put(name, value);
				}

				if(name.startsWith("send")){
					alert_send.put(name, value);
				}

				if(name.startsWith("stop")){
					alert_stop.put(name, value);
				}
			}

			// logfiles
			NodeList logfile_nodes = (NodeList) xpath.evaluate("//config/logfile", document, XPathConstants.NODESET);
			for(int lf_idx = 0; lf_idx < logfile_nodes.getLength(); lf_idx++){
				Node logfile_node = logfile_nodes.item(lf_idx);
				LogSource logsource = new LogSource();

				// FileName
				Node file_node = (Node) xpath.evaluate("file", logfile_node, XPathConstants.NODE);

				String filename = file_node.getTextContent();
				logger.fine("Read config of logfile " + filename);
				logsource.setFilename(filename);

				NamedNodeMap file_attr = file_node.getAttributes();
				Node start_node;
				if(file_attr != null && (start_node = file_attr.getNamedItem("start")) != null){

					try{
						StartPosition sp = StartPosition.valueOf(start_node.getTextContent().toUpperCase());
						logsource.setStartPosition(sp);

					} catch(IllegalArgumentException e){
						logger.warning("Wrong value in <file> tag");
					}
				}

				// Pattern
				NodeList pattern_nodes = (NodeList) xpath.evaluate("pattern", logfile_node, XPathConstants.NODESET);
				for(int p_idx = 0; p_idx < pattern_nodes.getLength(); p_idx++){
					Node pattern_node = pattern_nodes.item(p_idx);
					LogPattern logpattern = new LogPattern();

					Node regex_node = (Node) xpath.evaluate("regex", pattern_node, XPathConstants.NODE);
					String regex = regex_node.getTextContent();
					logpattern.setPattern(regex);

					Node msg_node = (Node) xpath.evaluate("msg", pattern_node, XPathConstants.NODE);
					String msg = msg_node.getTextContent();
					logpattern.setMessage(msg);

					Node sev_node = (Node) xpath.evaluate("severity", pattern_node, XPathConstants.NODE);
					String sev = sev_node.getTextContent();
					logpattern.setSeverity(sev);

					Node con_node = (Node) xpath.evaluate("condition", pattern_node, XPathConstants.NODE);
					if(con_node != null){
						String con_file = con_node.getTextContent();
						logpattern.setConditionFile(con_file);
					}

					NodeList pattern_prop_nodes = (NodeList) xpath.evaluate("properties/property", pattern_node, XPathConstants.NODESET);
					Properties pattern_properties = new Properties();

					for(int idx = 0; idx < pattern_prop_nodes.getLength(); idx++){
						Node prop_node = pattern_prop_nodes.item(idx);
						Node name_node = prop_node.getAttributes().getNamedItem("name");

						String value = prop_node.getTextContent();

						pattern_properties.put(name_node.getTextContent(), value);
					}
					logpattern.setSendProperties(pattern_properties);

					// Add pattern to list
					logsource.addPattern(logpattern);
				}

				logsources.add(logsource);
			}

		} catch(Exception e){
			logger.log(Level.SEVERE, "Can't read configuration", e);
			System.exit(1);
		}
	}

	/**
	 * Get List of LogSource
	 *
	 * @return All LogSource from configuration file
	 *
	 * @see LogSource()
	 */
	public List<LogSource> getLogSources() {
		return logsources;
	}

	/*
	 * @see cfg.ConfigMBean#getDBID()
	 */
	@Override
	public String getDBID() {
		return db_id;
	}

	/*
	 * @see cfg.ConfigMBean#getDBPath()
	 */
	@Override
	public String getDBPath() {
		return db_path;
	}

	/*
	 * @see cfg.ConfigMBean#getAlertClass()
	 */
	@Override
	public String getAlertClass() {
		return alertclass;
	}

	/*
	 * @see cfg.ConfigMBean#getAlertInit()
	 */
	@Override
	public Properties getAlertInit() {
		return alert_init;
	}

	/*
	 * @see cfg.ConfigMBean#getAlertSend()
	 */
	@Override
	public Properties getAlertSend() {
		return alert_send;
	}

	/*
	 * @see cfg.ConfigMBean#getAlertStop()
	 */
	@Override
	public Properties getAlertStop() {
		return alert_stop;
	}

	/*
	 * @see cfg.ConfigMBean#getConfigFileName()
	 */
	@Override
	public String getConfigFileName() {
		return config_file.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see cfg.ConfigMBean#getPIDPath()
	 */
	@Override
	public String getPIDPath() {
		return pid_path;
	}
}
