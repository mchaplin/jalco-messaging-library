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
import javax.jms.JMSException;
import net.sfr.tv.api.NamedObject;
import net.sfr.tv.jms.cnxmgt.ProducerConnectionManager;
import net.sfr.tv.jms.context.ProducerJmsContext;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class MessageProducerPool implements NamedObject {

    private static final Logger logger = Logger.getLogger(MessageProducerPool.class.getName());
    
    private final String name;
    
    private final Queue<ProducerConnectionManager> connectionManagers;
    
    private final Integer capacity = 10;
    
    private Map<String, BlockingDeque<ProducerJmsContext>> pool =  new HashMap<String, BlockingDeque<ProducerJmsContext>>(); // TODO : Synchronize access ?
    
    public MessageProducerPool(
            String name, 
            Set<MessagingServerDescriptor> servers, 
            String preferredServer, 
            String clientId, 
            String cnxFactoryJndiName, 
            Credentials credentials,
            Integer connections) {
                
        this.name = name;
        ProducerConnectionManager ocm;
        connectionManagers = new ArrayBlockingQueue<ProducerConnectionManager>(connections);
        for (int i=0 ; i<connections ; i++) {
            ocm = new ProducerConnectionManager(name.concat("-jms-pool-").concat(String.valueOf(i)), servers, preferredServer, clientId.concat("-").concat(String.valueOf(i)), cnxFactoryJndiName, credentials);
            connectionManagers.add(ocm);
        }
    }
    
    /*public getSize() {
    }*/
    
    @Override
    public String getName() {
        return name;
    }
    
    //@Override
    private ProducerJmsContext create(String pk) {
        
        ProducerConnectionManager ocm = connectionManagers.poll();
        if (ocm == null) {
            return null;
        }
        ProducerJmsContext ret = ocm.createProducer(pk);
        connectionManagers.add(ocm);
        
        return ret;
    }
    
    
    public ProducerJmsContext borrow(String key) {
        
        ProducerJmsContext ret;
        
        if (pool.containsKey(key)) {
            BlockingDeque<ProducerJmsContext> deck = pool.get(key);
            if (deck.isEmpty()) {
                if (deck.size() < capacity) {
                    return create(key);
                } else {
                    logger.warn("Ressource shortage for destination : ".concat(key).concat(" !"));
                }
            }
            return deck.removeFirst();
            
        } else {
            ret = create(key);
            BlockingDeque<ProducerJmsContext> deck = new LinkedBlockingDeque<ProducerJmsContext>();
            pool.put(key, deck);
            return ret;
        }
    }
    
    public void release(String key, ProducerJmsContext instance) {
        pool.get(key).addLast(instance);
    }
    
    public void invalidate(String key, ProducerJmsContext instance) {
        
        logger.info("Invalidating connection toward destination : ".concat(key));
        
        if (instance == null) {
            return;
        }
         
        for (BlockingDeque<ProducerJmsContext> deck : pool.values()) {
            for (ProducerJmsContext ctx : deck) {
                if (ctx.getParentName().equals(instance.getParentName())) {
                    try {
                        instance.getProducer().close();
                    } catch (JMSException ex) {
                        logger.error(ex.getMessage());
                    }
                    deck.remove(ctx);
                }
            }
        }
    }
    
    public void shutdown() {
        for (ProducerConnectionManager ocm : connectionManagers) {
            shutdownProducers(ocm);
            ocm.disconnect();
        }
    }
    
    private void shutdownProducers(ProducerConnectionManager ocm) {
        
        for (BlockingDeque<ProducerJmsContext> deck : pool.values()) {
            for (ProducerJmsContext ctx : deck) {
                if (ctx.getParentName().equals(ocm.getName())) {
                    try {
                        ctx.getProducer().close();
                    } catch (JMSException ex) {
                        logger.error(ex.getMessage());
                    }
                    deck.remove(ctx);
                }
            }
        }
    }
}