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

import net.sfr.tv.messaging.api.SubscriptionContext;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import org.hornetq.api.core.client.ClientConsumer;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreSubscription extends SubscriptionContext {
    
    private final ClientConsumer consumer;
    
    public HqCoreSubscription(final SubscriptionDescriptor descriptor, final String subscriptionName, final ClientConsumer consumer) {
        super(descriptor, subscriptionName);
        this.consumer = consumer;
    }

    public ClientConsumer getConsumer() {
        return consumer;
    }
}