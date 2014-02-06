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
package net.sfr.tv.jms.cnxmgt.pool;

import java.util.HashSet;
import java.util.Set;
import javax.jms.JMSException;
import net.sfr.tv.jms.cnxmgt.OutboundConnectionManager;
import net.sfr.tv.jms.context.OutboundJmsContext;
import net.sfr.tv.jms.model.JndiServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;
import org.pacesys.kbop.IPoolObjectFactory;
import org.pacesys.kbop.PoolKey;

/**
 *
 * @author matthieu
 */
public class MessageProducerPoolObjectFactory implements IPoolObjectFactory<String,OutboundJmsContext> {

    private static final Logger LOGGER = Logger.getLogger(MessageProducerPoolObjectFactory.class.getName());
    
    private final Set<OutboundConnectionManager> connectionManagers = new HashSet<>();
    
    private OutboundConnectionManager activeConnectionManager;
    
    private final String name;
    
    private final Set<JndiServerDescriptor> servers;
    
    private final String preferredServer;
    
    private final String clientIdPrefix;
    
    private final String cnxFactoryJndiName;
    
    private final Credentials credentials;
    
    public MessageProducerPoolObjectFactory(String name, Set<JndiServerDescriptor> servers, String preferredServer, String clientId, String cnxFactoryJndiName, Credentials credentials) {
        this.name = name;
        this.servers = servers;
        this.preferredServer = preferredServer;
        this.clientIdPrefix = clientId;
        this.cnxFactoryJndiName = cnxFactoryJndiName;
        this.credentials = credentials;
    }
    
    @Override
    public OutboundJmsContext create(PoolKey<String> pk) {
        // LED TO HQ214021: Invalid concurrent session usage. Sessions are not supposed to be used by more than one thread concurrently
        //OutboundConnectionManager connectionManager = new OutboundConnectionManager(name, servers, preferredServer, clientId, cnxFactoryJndiName, credentials);
        if (activeConnectionManager == null) {
            activeConnectionManager = new OutboundConnectionManager(name, servers, preferredServer, clientIdPrefix.concat("-").concat(String.valueOf(connectionManagers.size())), cnxFactoryJndiName, credentials);
            connectionManagers.add(activeConnectionManager);
        }

        OutboundJmsContext ret = activeConnectionManager.createProducer(pk.get());
        
        if (ret == null) {
            activeConnectionManager = new OutboundConnectionManager(name, servers, preferredServer, clientIdPrefix.concat("-").concat(String.valueOf(connectionManagers.size())), cnxFactoryJndiName, credentials);
            connectionManagers.add(activeConnectionManager);
            ret = activeConnectionManager.createProducer(pk.get());
        }
        
        return ret;
    }

    @Override
    public void activate(OutboundJmsContext v) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void passivate(OutboundJmsContext v) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void destroy(OutboundJmsContext v) {
        LOGGER.info("Destroying OutboundJmsContext : " + v.toString());
        try {
            v.getProducer().close();
        } catch (JMSException ex) {
            LOGGER.error("Unable to properly close MessageProducer !", ex);
        }
    }
}
