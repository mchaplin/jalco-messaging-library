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

import java.util.HashSet;
import java.util.Set;
import net.sfr.tv.messaging.api.context.ConsumerContext;
import net.sfr.tv.messaging.api.context.Context;
import net.sfr.tv.messaging.api.context.SubscriptionContext;
import org.hornetq.api.core.client.ClientSession;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HqCoreContext extends Context<ClientSession> implements ConsumerContext<SubscriptionContext> {
    
    //private final ClientSession session;
    
    private final Set<SubscriptionContext> subscriptions;
    
    public HqCoreContext(final ClientSession session) {
        this.session = session;
        this.subscriptions = new HashSet<>();
    }

    @Override
    public ClientSession getSession() {
        return session;
    }

    @Override
    public Set<SubscriptionContext> getSubscriptions() {
        return subscriptions;
    }
}
