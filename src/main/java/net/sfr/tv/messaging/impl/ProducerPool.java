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
package net.sfr.tv.messaging.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import net.sfr.tv.api.NamedObject;
import net.sfr.tv.messaging.api.MessageProducer;
import net.sfr.tv.messaging.api.connection.ProducerConnectionManager;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public abstract class ProducerPool implements NamedObject {

    private final Map<String, BlockingDeque<MessageProducer>> pool =  new HashMap<>(); // TODO : Synchronize access ?
    
    protected static final Logger logger = Logger.getLogger(ProducerPool.class.getName());
    
    protected String name;
    
    protected Queue<ProducerConnectionManager> connectionManagers;
    
    protected final Integer capacity = 10;
    
    public ProducerPool(
            String name,
            Integer connections) {
                
        this.name = name;
        connectionManagers = new ArrayBlockingQueue<>(connections);
    }
    
    /*public getSize() {
    }*/
    
    @Override
    public String getName() {
        return name;
    }
    
    //@Override
    private MessageProducer create(String pk) {
        
        ProducerConnectionManager ocm = connectionManagers.poll();
        if (ocm == null) {
            return null;
        }
        MessageProducer ret = ocm.createProducer(pk);
        connectionManagers.add(ocm);
        
        return ret;
    }
    
    
    public MessageProducer borrow(String key) {
        
        MessageProducer ret;
        
        if (pool.containsKey(key)) {
            BlockingDeque<MessageProducer> deck = pool.get(key);
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
            BlockingDeque<MessageProducer> deck = new LinkedBlockingDeque<>();
            pool.put(key, deck);
            return ret;
        }
    }
    
    //public void release(String key, PooledObject<T> instance) {
    public void release(String key, MessageProducer instance) {
        pool.get(key).addLast(instance);
    }
    
    public void invalidate(String key, MessageProducer instance) {
        
        logger.info("Invalidating connection toward destination : ".concat(key));
        
        if (instance == null) {
            return;
        }
         
        for (BlockingDeque<MessageProducer> deck : pool.values()) {
            for (MessageProducer ctx : deck) {
                if (ctx.getParentName().equals(instance.getParentName())) {
                    //try {
                        instance.close();
                    /*} catch (JMSException ex) {
                        logger.error(ex.getMessage());
                    }*/
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
        
        for (BlockingDeque<MessageProducer> deck : pool.values()) {
            for (MessageProducer ctx : deck) {
                if (ctx.getParentName().equals(ocm.getName())) {
                    //try {
                        ctx.close();
                    /*} catch (JMSException ex) {
                        logger.error(ex.getMessage());
                    }*/
                    deck.remove(ctx);
                }
            }
        }
    }
}