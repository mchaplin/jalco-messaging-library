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
package net.sfr.tv.messaging.api;

import java.util.List;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public interface MessageProducer {
   
    /**
     * @return Logical name of the ConnectionManager that created this instance.
     */
    String getParentName();
    
    /**
     * @return Tests for producer validity.
     */
    Boolean isValid();
    
    /**
     * Flag this instance as invalid, ie. not to be reused (In a pooling context)
     */
    void invalidate();
    
    void close();
    
    void sendTextMessage(List<MessageProperty> properties, String text) throws MessagingException;
    
    void sendBytesMessage(List<MessageProperty> properties, byte[] buffer) throws MessagingException;
}
