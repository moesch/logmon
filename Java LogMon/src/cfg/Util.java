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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class Util {

    /**
     * Resolv $ENV{}
     * 
     * 
     * @param in : String with $ENV{} entry's
     * 
     * @return String with resolved environment variables
     */
    static public String resolvENV(String in) {

        int pos = 0;
        StringBuilder sb = new StringBuilder();

        Pattern env_pattern = Pattern.compile("\\$ENV\\{([\\w\\d]+)\\}");
        Matcher env_matcher = env_pattern.matcher(in);
        while (env_matcher.find()) {
            String var = env_matcher.group(1);
            String value = System.getenv(var);

            // Add static part from current pos to placeholder
            sb.append(in.substring(pos, env_matcher.start()));
            pos = env_matcher.end();

            // Substitute placeholder
            sb.append(value);

        }
        sb.append(in.substring(pos, in.length()));

        return sb.toString();
    }
}
