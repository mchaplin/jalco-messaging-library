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
package net.sfr.tv.messaging.api.context;

import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.impl.ConsumerWrapper;

/**
 * A subscription context. References :
 * <ul>
 *  <li> A descriptor, holds subscription metadata.
 *  <li> An optional subscription name.
 *  <li> A wrapper to the message handler class.
 * </ul>
 * 
 * @author matthieu.chaplin@sfr.com
 * @param <T>
 */
public class SubscriptionContext<T> {
 
    public final SubscriptionDescriptor descriptor;
    
    public final String name;
    
    public final ConsumerWrapper<T> consumer;
    
    public SubscriptionContext(final SubscriptionDescriptor descriptor, final String name, final ConsumerWrapper<T> consumer) {
        this.descriptor = descriptor;
        this.name = name;
        this.consumer = consumer;
    }
}