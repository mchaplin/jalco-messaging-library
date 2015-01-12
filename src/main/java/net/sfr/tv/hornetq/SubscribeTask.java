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

import java.util.concurrent.Callable;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import org.apache.log4j.Logger;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.MessageHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class SubscribeTask implements Callable<HqCoreContext> {

    private static final Logger logger = Logger.getLogger(net.sfr.tv.jms.cnxmgt.SubscribeTask.class);
    
    public final HqCoreContext context;
    
    public final SubscriptionDescriptor descriptor;
    
    public final MessageHandler msgHandler;
    
    public SubscribeTask(final HqCoreContext context, final SubscriptionDescriptor descriptor, final MessageHandler msgHandler) {
        this.context = context;
        this.descriptor = descriptor;
        this.msgHandler = msgHandler;
    }
    
    @Override
    public HqCoreContext call() throws Exception {
        
        ClientConsumer messageConsumer = context.session.createConsumer(descriptor.getDestination());
        messageConsumer.setMessageHandler(msgHandler);
        context.subscriptions.add(new HqCoreSubscription(descriptor, descriptor.getSubscriptionName(), messageConsumer));
        
        return context;
    }
}
