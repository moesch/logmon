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

package alert;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class TestMail {
    Properties p = new Properties();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        
        p.put("init.smtp.server", "localhost");
        p.put("init.smtp.from", "logmon@die-moesch.de");
    
        p.put("send.smtp.to", "thomas@die-moesch.de");
        p.put("send.smtp.subject", "JUnit Test Case");
        
        p.put("send.severity", "WARNING");
        p.put("send.hostname", "localhost");
        p.put("send.msg", "Hallo Welt");
    }

    /**
     * Test method for {@link alert.SendMail#init(java.util.Properties)}.
     */
    @Test
    public void testInit() {
        SendMail sm = new SendMail();
        
        Assert.assertTrue( sm.init(p));
    }

    /**
     * Test method for {@link alert.SendMail#send(java.util.Properties)}.
     */
    @Test
    public void testSend() {
        SendMail sm = new SendMail();
        sm.init(p);
        
        Assert.assertTrue( sm.send(p));
    }

    /**
     * Test method for {@link alert.SendMail#stop()}.
     */
    @Test
    public void testStop() {
        SendMail sm = new SendMail();
        
        Assert.assertTrue( sm.stop());
    }

}
