package net.openesb.standalone.security.realm;

import java.util.Map;
import java.util.ServiceLoader;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class RealmBuilder {
    
    private final static String REALM_TYPE = "type";
    
    public static RealmBuilder realmBuilder() {
        return new RealmBuilder();
    }
    
    public Realm build(String realmName, Map<String, String> properties) {
        ServiceLoader<RealmHandler> handlers = ServiceLoader.load(RealmHandler.class);
        for(RealmHandler handler : handlers) {
            String type = properties.get(REALM_TYPE);
            if (handler.canHandle(type)) {
                Realm realm = handler.create(realmName, properties);
                
                return realm;
            }
        }
        
        throw new IllegalStateException("Unable to create realm " + realmName + 
                " : no handler found !");
    }
}
