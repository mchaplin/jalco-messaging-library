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
package net.sfr.tv.jms.context;

import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import net.sfr.tv.messaging.api.context.SubscriptionContext;
import net.sfr.tv.messaging.impl.ConsumerWrapper;

/**
 * A JMS subscription is composed of :
 * <ul>
 *  <li> metadata : Subscription name prefix, durability, selector, destination type
 *  <li> a destination
 *  <li> a listener, consuming messages
 * </ul>
 * 
 * @author matthieu.chaplin@sfr.com
 */
public class JmsSubscriptionContext extends SubscriptionContext<MessageConsumer> {
    
    private final Destination destination;
    
    public JmsSubscriptionContext(final SubscriptionDescriptor descriptor, final String subscriptionName, final Destination dst, final ConsumerWrapper<MessageConsumer> consumer) {
        super(descriptor, subscriptionName, consumer);
        this.destination = dst;
        this.consumer = consumer;
    }

    public Destination getDestination() {
        return destination;
    }
}
