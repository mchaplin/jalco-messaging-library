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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import net.sfr.tv.jms.cnxmgt.OutboundConnectionManager;
import net.sfr.tv.jms.context.OutboundJmsContext;
import net.sfr.tv.jms.model.JndiServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu
 */
public class MessageProducerPool {

    private static final Logger LOGGER = Logger.getLogger(MessageProducerPool.class.getName());
    
    private final Queue<OutboundConnectionManager> connectionManagers;
    
    private final Integer capacity = 10;
    
    private Map<String, BlockingDeque<OutboundJmsContext>> pool =  new HashMap<>(); // TODO : Synchronize access ?
    
    public MessageProducerPool(
            String name, 
            Set<JndiServerDescriptor> servers, 
            String preferredServer, 
            String clientId, 
            String cnxFactoryJndiName, 
            Credentials credentials,
            Integer connections) {
                
        connectionManagers = new ArrayBlockingQueue<>(connections);
        for (int i=0 ; i<connections ; i++) {
            connectionManagers.add(new OutboundConnectionManager(name, servers, preferredServer, clientId.concat("-").concat(String.valueOf(i)), cnxFactoryJndiName, credentials));
        }
    }
    
    /*public getSize() {
        
    }*/
    
    //@Override
    private OutboundJmsContext create(String pk) {
        
        OutboundConnectionManager ocm = connectionManagers.remove();
        OutboundJmsContext ret = ocm.createProducer(pk);
        connectionManagers.add(ocm);
        
        return ret;
    }
    
    
    public OutboundJmsContext borrow(String key) {
        
        OutboundJmsContext ret;
        
        if (pool.containsKey(key)) {
            BlockingDeque<OutboundJmsContext> deck = pool.get(key);
            if (deck.isEmpty()) {
                if (deck.size() < capacity) {
                    return create(key);
                } else {
                    LOGGER.warn("Ressource shortage for destination : ".concat(key).concat(" !"));
                }
            }
            return deck.removeFirst();
            
        } else {
            ret = create(key);
            BlockingDeque<OutboundJmsContext> deck = new LinkedBlockingDeque<>();
            pool.put(key, deck);
            return ret;
        }
    }
    
    public void release(String key, OutboundJmsContext instance) {
        pool.get(key).addLast(instance);
    }
    
    public void shutdown() {
        for (OutboundConnectionManager ocm : connectionManagers) {
            ocm.disconnect();
        }
    }
}
