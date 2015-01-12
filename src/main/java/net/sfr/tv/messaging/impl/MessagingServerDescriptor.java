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
package net.sfr.tv.messaging.impl;

/**
 * Messaging server URI.
 * 
 * @author matthieu.chaplin@sfr.com
 */
public class MessagingServerDescriptor {
 
    public final String alias;
    
    public final String host;
    
    public final Integer jndiPort;
    
    public final Integer hqTransportPort;
    
    public MessagingServerDescriptor(final String alias, final String host, final Integer jndiPort, final Integer hqTransportPort) {
        this.alias = alias;
        this.host = host;
        this.jndiPort = jndiPort;
        this.hqTransportPort = hqTransportPort;
    }
    
    /**
     * Returns the JNDI provider URL
     * 
     * @return 
     */
    public String getProviderUrl() {
        return "jnp://".concat(host).concat(":").concat(jndiPort.toString());
    }

    @Override
    public String toString() {
        return "Messaging provider : ".concat(alias).concat(" -> URL : ").concat(getProviderUrl());
    }

    @Override
    public boolean equals(Object obj) {
        if (MessagingServerDescriptor.class.isAssignableFrom(obj.getClass())) {
            return ((MessagingServerDescriptor) obj).getProviderUrl().compareTo(getProviderUrl()) == 0;
        } else {
            return super.equals(obj);
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (host != null ? host.hashCode() : 0) + (jndiPort != null ? jndiPort.hashCode() : 0);
        return hash;
    }
}