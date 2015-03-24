/**
 * Copyright 2012,2013 - SFR (http://www.sfr.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sfr.tv.jms.cnxmgt;

import net.sfr.tv.jms.cnxmgt.tasks.JndiLookupTask;
import net.sfr.tv.jms.cnxmgt.tasks.ConnectTask;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import net.sfr.tv.jms.context.JmsContext;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.messaging.impl.AbstractConnectionManager;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 * Stateful management of JMS connection, handling failover & reconnections
 * 
 * @author matthieu.chaplin@sfr.com
 * @param <T>
 */
public abstract class JmsConnectionManager<T extends JmsContext> extends AbstractConnectionManager implements ExceptionListener {
    
    private static final Logger logger = Logger.getLogger(JmsConnectionManager.class);
    
    /** JMS Connection Factory JNDI name */
    private final String cnxFactoryJndiName;
    
    /** JMS ClientID */
    private final String clientId;
    
    /** Current JNDI context */
    protected Context jndiContext;
    
    /** Currently used JMS resources */
    protected T context;
    
    public JmsConnectionManager(final String name, final Set<MessagingServerDescriptor> servers, final String preferredServer, final String clientId, final String cnxFactoryJndiName, final Credentials credentials) {
        super(name, credentials, servers, preferredServer);
        this.clientId = clientId;
        this.cnxFactoryJndiName = cnxFactoryJndiName;
        
        lookup(activeServer, 2, TimeUnit.SECONDS);
        
        logger.info(name.concat(" : Service provider URL : ").concat(activeServer.getProviderUrl()));
    }

    @Override
    public void lookup(long delay, TimeUnit tu) {
        this.lookup(activeServer, delay, tu);
    }
    
    @Override
    public final void lookup(MessagingServerDescriptor jndiServer, long delay, TimeUnit tu) {
        ScheduledFuture<Context> futureContext = null;
        JndiLookupTask jlt;
        boolean initConnect = true;
        try {
            while (futureContext == null || (jndiContext = futureContext.get()) == null) {
                // reschedule a task
                jlt = new JndiLookupTask(jndiServer);
                futureContext = scheduler.schedule(jlt, initConnect ? 0 : delay, tu);
                initConnect = false;
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(name.concat(" : ").concat(ex.getMessage()).concat(" : Caused by : ").concat(ex.getCause().getMessage()));
        }
    }
    
    /**
     * Establish a JMS connection and session.
     * 
     * @param delay     Periodic attempts delay.
     * @param tu
     * 
     */
    @Override
    public final void connect(long delay, TimeUnit tu) {
        ScheduledFuture<T> futureContext = null;
        ConnectTask ct;
        boolean initConnect = true;
        try {
            while (futureContext == null || (context = futureContext.get()) == null) {
                // reschedule a task
                ct = new ConnectTask<>(jndiContext, clientId, cnxFactoryJndiName, credentials, this);
                futureContext = scheduler.schedule(ct, initConnect ? 0 : delay, TimeUnit.SECONDS);
                initConnect = false;
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(name.concat(" : ").concat(ex.getMessage()).concat(" : Caused by : ").concat(ex.getCause().getMessage()));
        }
    }
    
    @Override
    public void disconnect() {
      
        logger.info(name.concat(" : Disconnecting.."));
        
        // TERMINATE SESSION
        if (context.getSession() != null) {
            try {
                ((Session) context.getSession()).close();
            } catch (JMSException ex) {
                logger.warn(ex.getMessage());
            }
        }
        
        // CLOSE CONNECTION
        if (context.connection != null) {
            try {
                context.connection.stop();
                context.connection.close();
            } catch (JMSException ex) {
                logger.warn(ex.getMessage());
            }
        }
    }

    @Override
    public void onException(JMSException e) {
        
        logger.error(name.concat(" : onException : ").concat(e.getMessage()));
        
        if (e.getMessage().toUpperCase().contains("DISCONNECTED")) {

            // BLACKLIST ACTIVE SERVER
            logger.error(name.concat(" : Active Server not available anymore ! ").concat(activeServer.getProviderUrl()));
            if (availableServers.size() > 1) {
                for (MessagingServerDescriptor srv : availableServers) {
                    if (!srv.equals(activeServer)) {
                        activeServer = srv;
                        break;
                    }
                }            
            }

            // LOOKUP NEW JNDI CONTEXT
            lookup(activeServer, 2, TimeUnit.SECONDS);
            logger.info(name.concat(" : service provider URL : ").concat(activeServer.getProviderUrl()));

            // CONNECT TO NEW ACTIVE SERVER WITH A 2 SECONDS PERIOD.
            connect(2, TimeUnit.SECONDS);   
        }
    }
    
    /*public final String getName() {
        return name;
    }

    public final String getJndiConnectionFactory() {
        return cnxFactoryJndiName;
    }

    public final Context getJndiContext() {
        return jndiContext;
    }

    public final JmsContext getJmsContext() {
        return context;
    }

    public final String getClientId() {
        return clientId;
    }*/
}