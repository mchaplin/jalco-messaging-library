/*
 * Copyright 2015 matthieu.chaplin@sfr.com.
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

import java.util.List;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import net.sfr.tv.jms.context.JmsContext;
import net.sfr.tv.messaging.api.MessageProducer;
import net.sfr.tv.messaging.api.MessageProperty;
import net.sfr.tv.messaging.api.MessagingException;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class JmsMessageProducer extends JmsContext implements MessageProducer {

    private Boolean valid = Boolean.TRUE;

    /**
     * Name of the OutboundConnectionManager that created this context.
     */
    private final String parentName;

    private final javax.jms.MessageProducer producer;

    public JmsMessageProducer(final String parentName, final Context jndiContext, final Connection cnx, final Session session, final javax.jms.MessageProducer producer) {
        super(jndiContext, cnx, session);
        this.parentName = parentName;
        this.producer = producer;
    }

    @Override
    public String getParentName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = Boolean.FALSE;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendTextMessage(List<MessageProperty> properties, String text) throws MessagingException {
        try {
            // submit JMS message
            TextMessage tm = getSession().createTextMessage();
            
            for(MessageProperty prop : properties) {
                switch (prop.type) {
                    case STRING:
                        tm.setStringProperty(prop.name, (String) prop.value);
                        break;
                }
            }
            
            tm.setText(text);
            
            producer.send(tm, DeliveryMode.NON_PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, 60 * 60 * 1000);
        } catch (JMSException ex) {
            throw new MessagingException(ex);
        }
    }

    @Override
    public void sendBytesMessage(List<MessageProperty> properties, byte[] buffer) throws MessagingException {

        try {
            // submit JMS message
            BytesMessage bm = getSession().createBytesMessage();

            for (MessageProperty prop : properties) {
                switch (prop.type) {
                    case LONG:
                        bm.setLongProperty(prop.name, (Long) prop.value);
                        break;
                }
            }
            bm.writeBytes(buffer);

            producer.send(bm, DeliveryMode.NON_PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, 60 * 60 * 1000);
        } catch (JMSException jMSException) {
            throw new MessagingException(jMSException);
        }
    }

}
