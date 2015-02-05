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
package net.sfr.tv.messaging.api.connection;

import java.util.concurrent.TimeUnit;
import net.sfr.tv.messaging.api.context.SubscriptionContext;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.api.context.Context;

/**
 * Handles consumer-oriented operations.
 * 
 * @author matthieu.chaplin@sfr.com
 */
public interface ConsumerConnectionManager<T extends Context> extends ConnectionManager {
    
    /**
     * Subscribe to a JMS destination.
     * 
     * @param descriptor    Subscription descriptor.
     * @param delay         Periodic attempts delay.
     * @param tu            TimeUnit of the delay.
     */
    void subscribe(SubscriptionDescriptor descriptor, long delay, TimeUnit tu);
    
    void unsubscribe(T context, SubscriptionContext subscriptionContext);
}
