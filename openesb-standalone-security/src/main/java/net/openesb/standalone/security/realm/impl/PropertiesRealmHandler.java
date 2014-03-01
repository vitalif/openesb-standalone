package net.openesb.standalone.security.realm.impl;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PropertiesRealmHandler extends AbstractRealmHandler<PropertiesRealm> {
    
    private final Logger mLog =
            Logger.getLogger(this.getClass().getPackage().getName());
    
    private final static String PROPERTIES_REALM = "properties";

    private final static String PROPERTY_PATH = "file";
    private final static String PROPERTY_RELOAD_ENABLE = "reload";
    private final static String PROPERTY_RELOAD_INTERVAL = "interval";
    
    @Override
    public boolean canHandle(String type) {
        return PROPERTIES_REALM.equalsIgnoreCase(type);
    }

    @Override
    public PropertiesRealm create(Map<String, String> properties) {
        String file = properties.get(PROPERTY_PATH);
        file = replace(file);
        
        mLog.log(Level.INFO, "Creating properties realm using file: {0}", file);
        
        File propertyFile = new File(file);
        if (! propertyFile.exists()) {
            mLog.log(Level.SEVERE, "Properties realm, invalid path: {0}", 
                    propertyFile.getAbsolutePath());
            throw new IllegalStateException("Properties realm, invalid path: " +
                    propertyFile.getAbsolutePath());
        }
        
        boolean reload = Boolean.parseBoolean(properties.get(PROPERTY_RELOAD_ENABLE));
        PropertiesRealm propertiesRealm = new PropertiesRealm();
        propertiesRealm.setPath(propertyFile.getAbsolutePath());
        
        if (reload) {
            String sInterval = properties.get(PROPERTY_RELOAD_INTERVAL);
            try {
                int interval = Integer.parseInt(sInterval);
                propertiesRealm.setReloadInterval(interval);
            } catch (NumberFormatException nfe) {
                
            }
        }
        
        return propertiesRealm;
    }
}
