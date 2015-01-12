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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import net.sfr.tv.jms.context.ConsumerJmsContext;
import net.sfr.tv.jms.context.JmsSubscriptionContext;
import net.sfr.tv.messaging.api.ConsumerConnectionManager;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu
 */
public class JmsConsumerConnectionManager extends JmsConnectionManager implements ConsumerConnectionManager {
    
    private static final Logger logger = Logger.getLogger(JmsConsumerConnectionManager.class);
    
    /** TODO : Add support for a dedicated listener per subscription. JMS2 supports multiple listener instance.*/
    private final MessageListener listener;
    
    /** Allows to keep tracks of subscriptions during a reconnection */
    protected Set<SubscriptionDescriptor> previousSubscriptions;
    
    public JmsConsumerConnectionManager(String name, Set<MessagingServerDescriptor> servers, String preferredServer, String clientId, String cnxFactoryJndiName, Credentials credentials, MessageListener listener) {
        super(name, servers, preferredServer, clientId, cnxFactoryJndiName, credentials);
        this.listener = listener;
    }
    
    /**
     * Subscribe to a JMS destination.
     * 
     * @param metadata  Subscription metadata.
     * @param delay     Periodic attempts delay.
     */
    public final void subscribe(SubscriptionDescriptor metadata, long delay) {
        ScheduledFuture<ConsumerJmsContext> futureContext = null;
        SubscribeTask ct;
        boolean initConnect = true;
        try {
            while (futureContext == null || (context = futureContext.get()) == null) {
                // reschedule a task
                ct = new SubscribeTask(context, metadata, listener);
                futureContext = scheduler.schedule(ct, initConnect ? 0 : delay, TimeUnit.SECONDS);
                initConnect = false;
            }
            
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    /*public final void subscribe(String destination, boolean isTopicSubscription, boolean isDurableSubscription, String subscriptionBaseName, String selector) {
        subscribe(new SubscriptionDescriptor(destination, isTopicSubscription, isDurableSubscription, subscriptionBaseName, selector), 2);
    }*/

    @Override
    public void disconnect() {
        
        // UNSUBSCRIBE
        if (((ConsumerJmsContext) context).getSubscriptions() != null) {
            for (JmsSubscriptionContext subscription : ((ConsumerJmsContext) context).getSubscriptions()) {
                unsubscribe(subscription, context.getSession());
            }   
        }
        
        super.disconnect();
    }
    
    

    @Override
    public void onException(JMSException jmse) {
        
        logger.warn("onException : ".concat(jmse.getMessage()));
        
        if (jmse.getMessage().toUpperCase().indexOf("DISCONNECTED") != -1) {
            
            // KEEP TRACK OF PREVIOUS SUBSCRIPTIONS METADATA
            previousSubscriptions = new HashSet<SubscriptionDescriptor>();
            for (JmsSubscriptionContext subscription : ((ConsumerJmsContext) context).getSubscriptions()) {
                previousSubscriptions.add(subscription.getDescriptor());
            }

            // RECONNECT
            super.onException(jmse);

            // RESUME SUBSCRIPTION OVER NEW ACTIVE SERVER
            for (SubscriptionDescriptor meta : previousSubscriptions) {
                subscribe(meta, 5);
            }

            try {
                start();
            } catch (JMSException ex) {
                logger.error("Unable to start connection !", ex);
                for (JmsSubscriptionContext subscription : ((ConsumerJmsContext) context).getSubscriptions()) {
                    unsubscribe(subscription, context.getSession());
                }
            }
        }
    }
}
