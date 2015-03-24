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
package net.sfr.tv.messaging.impl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.sfr.tv.messaging.api.connection.ConnectionManager;
import net.sfr.tv.model.Credentials;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public abstract class AbstractConnectionManager implements ConnectionManager {
    
    protected final String name;
    
    /** Messaging server connection credentials */
    protected final Credentials credentials;
    
    /** Configuration reference */
    protected Set<MessagingServerDescriptor> availableServers;
    /** Active configuration reference */
    protected MessagingServerDescriptor activeServer;
    
    /** ExecutorService used for periodic tasks */
    protected final ScheduledExecutorService scheduler;
    
    protected AbstractConnectionManager(final String name, final Credentials credentials, final Set<MessagingServerDescriptor> availableServers, final String preferredServer) {
        this.name = name;
        this.credentials = credentials;
        this.availableServers = availableServers;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        if (preferredServer != null && preferredServer.trim().length() != 0) {
            for (MessagingServerDescriptor desc : availableServers) {
                if (desc.alias.equals(preferredServer)) {
                    activeServer = desc;
                    break;
                }
            }
        } else {
            // Try with 1st server. TODO : Optimize and round-robin
            this.activeServer = availableServers.iterator().next();
        }
    }
    
    
}
