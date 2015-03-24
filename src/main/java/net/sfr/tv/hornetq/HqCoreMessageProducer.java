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
package net.sfr.tv.hornetq;

import java.util.Date;
import java.util.List;
import net.sfr.tv.messaging.api.MessageProducer;
import net.sfr.tv.messaging.api.MessageProperty;
import net.sfr.tv.messaging.api.MessagingException;
/*import org.apache.activemq.api.core.ActiveMQException;
import org.apache.activemq.api.core.Message;
import org.apache.activemq.api.core.client.ClientProducer;
import org.apache.activemq.api.core.client.ClientSession;*/
import org.apache.log4j.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreMessageProducer extends HqCoreContext implements MessageProducer {

    private static final Logger logger = Logger.getLogger(HqCoreMessageProducer.class);
    
    private Boolean valid = Boolean.TRUE;
    
    public final ClientProducer producer;
    
    public HqCoreMessageProducer(final ClientSession session, final ClientProducer producer) {
        super(session);
        this.producer = producer;
    }

    @Override
    public Boolean isValid() {
        return valid;
    }

    @Override
    public void invalidate() {
        valid = Boolean.FALSE;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendTextMessage(List<MessageProperty> properties, String text) throws MessagingException {
        sendMessage(Message.BYTES_TYPE, properties, text.getBytes());
    }

    @Override
    public void sendBytesMessage(List<MessageProperty> properties, byte[] buffer) throws MessagingException {
        sendMessage(Message.BYTES_TYPE, properties, buffer);
    }
    
    private void sendMessage(byte type, List<MessageProperty> properties, byte[] buffer) throws MessagingException { 
        try {
            long timestamp = new Date().getTime();
            Message msg = session.createMessage(type, false, timestamp + (60 * 60 * 1000), timestamp, (byte) 4);
            for (MessageProperty prop : properties) {
                switch(prop.type) {
                    case STRING:
                        msg.putStringProperty(prop.name, (String) prop.value);
                        break;
                    case LONG:
                        msg.putLongProperty(prop.name, (Long) prop.value);
                        break;
                }
            }
            msg.getBodyBuffer().writeBytes(buffer);
            producer.send(msg);
        } catch (HornetQException ex) {
            throw new MessagingException(ex);
        }
    }
}
