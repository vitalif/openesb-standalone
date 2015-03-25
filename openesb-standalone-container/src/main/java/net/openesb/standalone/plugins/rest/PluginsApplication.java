package net.openesb.standalone.plugins.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginsApplication extends ResourceConfig {
    
    public PluginsApplication() {
        super(
                PluginsResource.class,
                JacksonFeature.class
        );
    }
    
}
