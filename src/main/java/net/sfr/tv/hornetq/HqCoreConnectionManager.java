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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.Session;
import net.sfr.tv.jms.context.JmsSubscriptionContext;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.api.ConnectionManager;
import net.sfr.tv.messaging.api.ConsumerConnectionManager;
import net.sfr.tv.messaging.impl.AbstractConnectionManager;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.MessageHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreConnectionManager extends AbstractConnectionManager implements ConnectionManager, ConsumerConnectionManager {
    
    private static final Logger logger = Logger.getLogger(HqCoreConnectionManager.class);

    protected ClientSessionFactory sessionFactory;
    
    /** Active context */
    private HqCoreContext context;
    
    /** Message handler instance, that process received messages */
    private final MessageHandler msgHandler;
    
    public HqCoreConnectionManager(final String name, final Credentials credentials, final Set<MessagingServerDescriptor> availableServers, final String preferredServer, final MessageHandler msgHandler) {
        super(name, credentials, availableServers, preferredServer);
        this.msgHandler = msgHandler;
        
        lookup(activeServer, 2);
        
        logger.info("Service provider URL : ".concat(activeServer.getProviderUrl()));
    }
    
    @Override
    public void lookup(MessagingServerDescriptor serverDescriptor, long delay) {
        ScheduledFuture<HqCoreContext> futureContext = null;
        HqCoreLookupTask jlt;
        boolean initConnect = true;
        try {
            while (futureContext == null || (this.context = futureContext.get()) == null) {
                // reschedule a task
                jlt = new HqCoreLookupTask(serverDescriptor);
                futureContext = scheduler.schedule(jlt, initConnect ? 0 : delay, TimeUnit.SECONDS);
                initConnect = false;
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            //logger.error(ex.getMessage().concat(" : Caused by : ").concat(ex.getCause() != null ? ex.getCause().getMessage() : ""));
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void connect(long delay) {
        return; // DOESN'T SEEM APPLICABLE FOR HQ CORE PROTOCOL
    }
    
    // DUPLICATE CODE FROM InboundConnectionManager
    
    /**
     * Subscribe to a JMS destination.
     * 
     * @param metadata  Subscription metadata.
     * @param delay     Periodic attempts delay.
     */
    @Override
    public final void subscribe(SubscriptionDescriptor metadata, long delay) {
        ScheduledFuture<HqCoreContext> futureContext = null;
        SubscribeTask ct;
        boolean initConnect = true;
        try {
            while (futureContext == null || (this.context = futureContext.get()) == null) {
                // reschedule a task
                ct = new SubscribeTask(this.context, metadata, msgHandler);
                futureContext = scheduler.schedule(ct, initConnect ? 0 : delay, TimeUnit.SECONDS);
                initConnect = false;
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    // DUPLICATE CODE FROM InboundConnectionManager

    @Override
    public void start() throws Exception {
        context.session.start();
        logger.debug("HornetQ session : " + context.session.toString() + " started");
    }
    
    @Override
    public void unsubscribe(JmsSubscriptionContext subscription, Session session) {
        logger.warn("Unimplemeted method : unsubscribe(JMSException jmse) !");
    }

    @Override
    public void disconnect() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    public void onException(JMSException jmse) {
        logger.warn("Unimplemeted method : onException(JMSException jmse) !");
    }
    
    
    
}
