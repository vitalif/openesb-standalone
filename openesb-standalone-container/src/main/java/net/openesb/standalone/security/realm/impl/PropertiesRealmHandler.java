package net.openesb.standalone.security.realm.impl;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.utils.I18NBundle;
import net.openesb.standalone.utils.StringUtils;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PropertiesRealmHandler extends AbstractRealmHandler<PropertiesRealm> {

    private final static Logger LOG =
            Logger.getLogger(PropertiesRealmHandler.class.getName());
    private final static String PROPERTIES_REALM = "properties";
    private final static String PROPERTY_PATH = "file";
    private final static String PROPERTY_RELOAD_ENABLE = "reload";
    private final static String PROPERTY_RELOAD_INTERVAL = "interval";

    @Override
    public boolean canHandle(String type) {
        return PROPERTIES_REALM.equalsIgnoreCase(type);
    }

    @Override
    public PropertiesRealm instantiate(String realmName, Map<String, String> properties) {
        String file = properties.get(PROPERTY_PATH);
        file = StringUtils.replace(file);

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.SECURITY_CREATE_PROPERTIES_REALM, file));
        }

        File propertyFile = new File(file);
        if (!propertyFile.exists()) {
            String msg = I18NBundle.getBundle().getMessage(
                    LocalStringKeys.SECURITY_CREATE_PROPERTIES_REALM_INVALID_PATH,
                    propertyFile.getAbsolutePath());
            
            LOG.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        boolean reload = Boolean.parseBoolean(properties.get(PROPERTY_RELOAD_ENABLE));
        PropertiesRealm propertiesRealm = new PropertiesRealm(realmName);
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
