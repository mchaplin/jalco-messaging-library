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
package net.sfr.tv.jms.context;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;

/**
 *
 * @author matthieu
 */
public class ProducerJmsContext extends JmsContext {
    
    /** Name of the OutboundConnectionManager that created this context. */
    private final String parentName;
    
    private final MessageProducer producer;
    
    private Boolean valid;
    
    public ProducerJmsContext(final String parentName, final Context jndiContext, final Connection cnx, final Session session, final MessageProducer producer) {
        super(jndiContext, cnx, session);
        this.parentName = parentName;
        this.producer = producer;
        this.valid = Boolean.TRUE;
    }

    public Boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = Boolean.FALSE;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public String getParentName() {
        return parentName;
    }
}
