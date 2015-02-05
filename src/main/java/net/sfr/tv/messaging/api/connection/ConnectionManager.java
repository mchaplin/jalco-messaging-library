
/*
 * Copyright 2014 matthieu.chaplin@sfr.com.
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
import javax.jms.JMSException;
import net.sfr.tv.messaging.impl.MessagingServerDescriptor;

/**
 * Handles messaging connections operations.
 * 
 * @author matthieu.chaplin@sfr.com
 */
public interface ConnectionManager {
    
    void lookup(MessagingServerDescriptor descriptor, long delay, TimeUnit tu);
    
    void connect(long delay, TimeUnit tu);
    
    void start() throws Exception;
    
    /**
     * Release a connection, terminating associated resources :
     * <ul>
     *  <li> Subscriptions
     *  <li> Sessions
     * </ul>
     */
    void disconnect();
    
    void onException(JMSException jmse);
    
}
