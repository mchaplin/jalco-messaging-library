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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;
/*import org.apache.activemq.api.core.TransportConfiguration;
import org.apache.activemq.api.core.client.ActiveMQClient;
import org.apache.activemq.api.core.client.ClientSessionFactory;
import org.apache.activemq.api.core.client.ServerLocator;
import org.apache.activemq.core.remoting.impl.netty.NettyConnectorFactory;*/
import org.apache.log4j.Logger;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreLookupTask implements Callable<ClientSessionFactory> {

    private static final Logger logger = Logger.getLogger(HqCoreLookupTask.class);
    
    private final MessagingServerDescriptor serverDescriptor;
    
    public HqCoreLookupTask(final MessagingServerDescriptor serverDescriptor) {
        this.serverDescriptor = serverDescriptor;
        logger.info("Looking up connection ressources on server : " + serverDescriptor.toString());
    }
    
    @Override
    public ClientSessionFactory call() throws Exception {
        
         Map<String,Object> map = new HashMap<>();
         map.put("host", serverDescriptor.host);
         map.put("port", serverDescriptor.hqTransportPort);
         // -------------------------------------------------------

         ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class.getName(), map));
         // TODO : MAKE CONFIGURABLE
         serverLocator.setPreAcknowledge(true);
         serverLocator.setBlockOnDurableSend(false);
         serverLocator.setConsumerMaxRate(-1);
         ClientSessionFactory sessionFactory = serverLocator.createSessionFactory();
         return sessionFactory;
    }
}
