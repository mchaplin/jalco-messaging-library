/*
 * Copyright 2012,2013 - SFR (http://www.sfr.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sfr.tv.jms.cnxmgt;

import java.util.Set;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;
import net.sfr.tv.jms.model.JndiServerDescriptor;
import net.sfr.tv.jms.context.OutboundJmsContext;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu
 */
public class OutboundConnectionManager extends AbstractConnectionManager {
 
    private static final Logger LOGGER = Logger.getLogger(OutboundConnectionManager.class);
    
    public OutboundConnectionManager(String name, Set<JndiServerDescriptor> servers, String preferredServer, String clientId, String cnxFactoryJndiName, Credentials credentials) {
        super(name, servers, preferredServer, clientId, cnxFactoryJndiName, credentials);
        
        connect(2);
    }
    
    public OutboundJmsContext createProducer(String destination) {
        
        Session session;
        MessageProducer producer;
        
        try {
            
            Destination dest = (Destination) jndiContext.lookup(destination);
            
            session = context.getConnection().createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            producer = session.createProducer(dest);
            
            // Set Delivery Mode (Durable, Non-Durable)
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // Disable messageId & timestamp, saves uniqueId & timestamp generation on the JMS server side
            producer.setDisableMessageID(true);
            //producer.setDisableMessageTimestamp(true);
            // Set TTL, afterwards message will be moved to an expiry queue
            producer.setTimeToLive(60 * 60 * 1000);
            
            LOGGER.info("Destination : ".concat(destination).concat(" : allocating a JMS producer."));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" Configuration : ");
                LOGGER.debug("\t Delivery Mode : " + producer.getDeliveryMode());
                LOGGER.debug("\t TTL : " + producer.getTimeToLive());
                LOGGER.debug("\t Message ID ? " + !producer.getDisableMessageID());
                LOGGER.debug("\t Message Timestamp ? " + !producer.getDisableMessageTimestamp());
            }
         
            return new OutboundJmsContext(getName(), context.getJndiContext(), context.getConnection(), session, producer);
            
        } catch (NamingException | JMSException ex) {
            LOGGER.error("Unable to create connection upon destination : ".concat(destination).concat(" ! Cause : ").concat(ex.getMessage()));
            return null;
        }
    }
}
