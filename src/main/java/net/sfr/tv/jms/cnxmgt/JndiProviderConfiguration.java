/*
 * Copyright 2012,2013 - SFR (http://www.sfr.com/)
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
package net.sfr.tv.jms.cnxmgt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.sfr.tv.jms.model.JndiServerDescriptor;
import net.sfr.tv.model.Credentials;
import org.apache.log4j.Logger;

/**
 *
 * @author matthieu
 */
public class JndiProviderConfiguration {
 
    private static final Logger LOGGER = Logger.getLogger(JndiProviderConfiguration.class.getName());
    
    private Credentials credentials;
    
    private String preferredServer; // TODO : By groups..
    
    private Set<String> groups = new HashSet<String>();
    
    private Map<String, Set<JndiServerDescriptor>> serversGroups;
    
    public JndiProviderConfiguration(Properties props, String service) {
        
        String[] sGroups = props.getProperty((service != null ? service.concat(".") : "").concat("config.groups"), "").split("\\,");

        String serverAlias;
        String keyPrefix;
        Set<String> keys = props.stringPropertyNames();
        
        JndiServerDescriptor server;

        serversGroups = new HashMap<String, Set<JndiServerDescriptor>>();
        
        for (String group : sGroups) {
            LOGGER.debug("Group : " + group);
            groups.add(group);
            
            group = !group.equals("") ? group : "default";
            keyPrefix = service != null && service.trim().length() != 0 ? service.concat(".").concat(group) : group;
            
            LOGGER.debug("Key prefix : " + keyPrefix);
            
            // AWFUL PARSING
            for (String key : keys) {
                if (key.startsWith(keyPrefix)) {
                    serverAlias = (service == null ? key.split("\\.")[3] : key.split("\\.")[4]);
                    LOGGER.debug("Server alias : " + serverAlias);
                    
                    if (serverAlias.equals("preferred")) {
                        preferredServer = props.getProperty(keyPrefix.concat(".jms.server.").concat(serverAlias));
                    } else {
                        
                        if (serversGroups.get(group) == null) {
                            serversGroups.put(group, new HashSet<JndiServerDescriptor>());
                        }

                        server = new JndiServerDescriptor(
                            serverAlias,
                            props.getProperty(keyPrefix.concat(".jms.server.").concat(serverAlias).concat(".host")),
                            Integer.valueOf(props.getProperty(keyPrefix.concat(".jms.server.").concat(serverAlias).concat(".port"))));

                        if (!serversGroups.get(group).contains(server)) {
                            serversGroups.get(group).add(server);    
                            LOGGER.info(server.toString());
                        }
                    }
                }
            }
        }

        credentials = new Credentials(props.getProperty("jms.login", "guest"), props.getProperty("jms.password", "guest"));   
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Map<String, Set<JndiServerDescriptor>> getServersGroup() {
        return serversGroups;
    }
    
    public Set<JndiServerDescriptor> getServersGroup(String name) {
        return serversGroups.get(name);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public String getPreferredServer() {
        return preferredServer;
    }
}
