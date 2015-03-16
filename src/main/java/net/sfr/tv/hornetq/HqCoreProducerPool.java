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
import java.util.concurrent.TimeUnit;
import net.sfr.tv.messaging.api.MessageProducer;
import net.sfr.tv.messaging.api.connection.ProducerConnectionManager;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
import net.sfr.tv.messaging.impl.ProducerPool;
import net.sfr.tv.model.Credentials;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreProducerPool extends ProducerPool {
    
    public HqCoreProducerPool(
            String name, 
            Set<MessagingServerDescriptor> servers, 
            String preferredServer,
            Credentials credentials) {
                
        super(name, 1);
        
        ProducerConnectionManager ocm = new HqCoreConnectionManager(name.concat("-hqcore-pool"), null, servers, preferredServer);
        ocm.lookup(2, TimeUnit.SECONDS);
        connectionManagers.add(ocm);
        
    }
    
    @Override
    protected MessageProducer create(String pk) {
        return connectionManagers.element().createProducer(pk);
    }
}