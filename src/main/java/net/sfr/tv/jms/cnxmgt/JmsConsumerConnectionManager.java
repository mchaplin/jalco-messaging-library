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

import net.sfr.tv.jms.cnxmgt.tasks.SubscribeTask;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import net.sfr.tv.jms.context.JmsConsumerContext;
import net.sfr.tv.jms.context.JmsSubscriptionContext;
import net.sfr.tv.messaging.api.connection.ConsumerConnectionManager;
import net.sfr.tv.messaging.api.context.SubscriptionContext;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu
 */
public class JmsConsumerConnectionManager extends JmsConnectionManager<JmsConsumerContext> implements ConsumerConnectionManager<JmsConsumerContext> {
    
    private static final Logger logger = Logger.getLogger(JmsConsumerConnectionManager.class);
    
    /** TODO : Add support for a dedicated listener per subscription. JMS2 supports multiple listener instance.*/
    private final MessageListener listener;
    
    /** Allows to keep tracks of subscriptions during a reconnection */
    protected Set<SubscriptionDescriptor> previousSubscriptions;
    
    public JmsConsumerConnectionManager(String name, Set<MessagingServerDescriptor> servers, String preferredServer, String clientId, String cnxFactoryJndiName, Credentials credentials, MessageListener listener) {
        super(name, servers, preferredServer, clientId, cnxFactoryJndiName, credentials);
        this.listener = listener;
    }
    
    @Override
    public final void subscribe(SubscriptionDescriptor descriptor, long delay, TimeUnit tu) {
        ScheduledFuture<JmsConsumerContext> futureContext = null;
        SubscribeTask ct;
        boolean initConnect = true;
        try {
            while (futureContext == null || (context = futureContext.get()) == null) {
                // reschedule a task
                ct = new SubscribeTask(context, descriptor, listener);
                futureContext = scheduler.schedule(ct, initConnect ? 0 : delay, tu);
                initConnect = false;
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    /**
     * Release a JMS subscription
     * 
     * @param subscription 
     * @param session
     */
    @Override
    public final void unsubscribe(JmsConsumerContext context, SubscriptionContext subscription) {

        if (logger.isDebugEnabled()) {
            logger.debug(getName().concat(" : About to unsubscribe : ").concat(subscription.getSubscriptionName()));
        }
        
        // CLOSE CONSUMTER
        if (subscription.getConsumer() != null) {
            try {
                javax.jms.MessageConsumer jmsConsumer = (javax.jms.MessageConsumer) subscription.getConsumer().getWrapped();
                jmsConsumer.close();
                subscription.getConsumer().release(); // UNIMPLEMENTED. PERFORMED ABOVE
            } catch (JMSException ex) {
                logger.warn(ex.getMessage());
            }

        }
        
        // UNSUBSCRIBE
        if (context.getSession() != null && subscription.getDescriptor().isIsTopicSubscription() && !subscription.getDescriptor().isIsDurableSubscription()) {
            // Unsubscribe, to prevent leaving a potential 'shadow' queue & permit reusing the same clientId later on.
            try {
                ((Session) context.getSession()).unsubscribe(subscription.getSubscriptionName());
                logger.info(getName().concat(" : Unsubscribed : ").concat(subscription.getSubscriptionName()));
            } catch (JMSException ex) {
                logger.error(getName().concat(ex.getMessage()).concat(" : Caused by : ").concat(ex.getCause() != null ? ex.getCause().getMessage() : ""));
            }
        }
    }

    @Override
    public void disconnect() {
        
        // UNSUBSCRIBE
        if (((JmsConsumerContext) context).getSubscriptions() != null) {
            for (JmsSubscriptionContext subscription : ((JmsConsumerContext) context).getSubscriptions()) {
                unsubscribe(context, subscription);
            }   
        }
        
        super.disconnect();
    }
       

    @Override
    public void onException(JMSException jmse) {
        
        logger.warn("onException : ".concat(jmse.getMessage()));
        
        if (jmse.getMessage().toUpperCase().contains("DISCONNECTED")) {
            
            // KEEP TRACK OF PREVIOUS SUBSCRIPTIONS METADATA
            previousSubscriptions = new HashSet<>();
            for (JmsSubscriptionContext subscription : ((JmsConsumerContext) context).getSubscriptions()) {
                previousSubscriptions.add(subscription.getDescriptor());
            }

            // RECONNECT
            super.onException(jmse);

            // RESUME SUBSCRIPTION OVER NEW ACTIVE SERVER
            for (SubscriptionDescriptor meta : previousSubscriptions) {
                subscribe(meta, 5, TimeUnit.SECONDS);
            }

            try {
                start();
            } catch (JMSException ex) {
                logger.error("Unable to start connection !", ex);
                for (JmsSubscriptionContext subscription : ((JmsConsumerContext) context).getSubscriptions()) {
                    unsubscribe(context, subscription);
                }
            }
        }
    }
}