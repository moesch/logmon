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

import java.io.*;

import org.xml.sax.*;

/**
 * Load config.dtd from classpath Add follow line to configuration xml:<br>
 * 
 * &lt;!DOCTYPE configuration SYSTEM &quot;configuration.dtd&quot;&gt;
 */
public class ConfigEnityResolver implements EntityResolver {

	/*
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		// read configuration.dtd from jar
		if(systemId.endsWith("configuration.dtd")){
			Reader reader = new InputStreamReader(getClass().getResource("/cfg/configuration.dtd").openStream());
			return new InputSource(reader);
		}

		return null;
	}

}
