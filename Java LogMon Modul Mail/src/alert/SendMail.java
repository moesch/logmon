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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import mon.evt.IAlert;


/**
 * Send Mail 
 */
public class SendMail implements IAlert{
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Properties mail_props = new Properties();
    private InternetAddress addressFrom=null;;

    private boolean readytosend=false;
    
    
    @Override
    public boolean init(Properties properties) {
        boolean rc=false;
        
        String server = properties.getProperty("init.smtp.server");
        String from = properties.getProperty("init.smtp.from");
        
        if(server!=null && from!=null){
            mail_props.put( "mail.smtp.host", server ); 
        
            try {
                addressFrom = new InternetAddress(from);
                rc = true;
            } catch (AddressException e) {
                logger.log(Level.SEVERE, "Problem with sender address", e);
            }
        }
        
        readytosend=rc;
        return rc;
    }

    @Override
    public boolean send(Properties properties) {
        boolean rc=false;

        logger.fine("Create mail with "+properties);

        // get required values
        String recipient = properties.getProperty("send.smtp.to");
        String subject = properties.getProperty("send.smtp.subject");
        
        // Build message
        StringBuilder sb = new StringBuilder();
        sb.append("Mail from LogMon\n\n");
        
        sb.append("Severity: ");
        sb.append(properties.getProperty("send.severity"));
        sb.append("\n");

        sb.append("Hostname: ");
        sb.append(properties.getProperty("send.hostname"));
        sb.append("\n");

        sb.append("Repeat: ");
        sb.append(properties.getProperty("send.repeat"));
        sb.append("\n");
        
        sb.append("-------------------------------------------------\n");
        
        sb.append("Message: ");
        sb.append(properties.getProperty("send.msg"));
        sb.append("\n");
        
        String message = sb.toString();

        if(readytosend && recipient!=null && subject!=null ){            
            try {
                // Setup session
                String user=properties.getProperty("init.smtp.user");
                String pwd=properties.getProperty("init.smtp.pwd");
                
                Session session; 
                if(user!=null && pwd!=null){
                    Authenticator auth = new SMTPAuthenticator(user,pwd);
                    
                    session = Session.getDefaultInstance(mail_props, auth);;
                }else{
                    
                    session = Session.getDefaultInstance( mail_props );
                }

                Message msg = new MimeMessage( session );

                // set from (set and check in init())
                msg.setFrom( addressFrom );
   
                // To:
                InternetAddress addressTo = new InternetAddress( recipient ); 
                msg.setRecipient( Message.RecipientType.TO, addressTo ); 
                
                // Subject:
                msg.setSubject( subject ); 
                
                // Message:
                msg.setContent( message, "text/plain" ); 
                
                // Send mail to smtp server
                logger.info("Send mail to "+recipient);
                Transport.send( msg ); 
                
                rc=true;
            } catch (MessagingException e) {
                logger.warning("Send mail exception "+e.getMessage() );
            } 
        }
        
        return rc;
    }

    @Override
    public boolean stop() {
        return true;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        private String user=null;
        private String pwd=null;
        
        public SMTPAuthenticator(String user, String pwd) {
            super();
            
            this.user=user;
            this.pwd=pwd;
        }

        public PasswordAuthentication getPasswordAuthentication() {
          
            if(user!=null && pwd!=null){
                return new PasswordAuthentication(user, pwd);
            }
            
           return null;
        }
    }
}
