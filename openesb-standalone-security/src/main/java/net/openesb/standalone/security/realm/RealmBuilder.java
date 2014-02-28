package net.openesb.standalone.security.realm;

import java.util.Map;
import java.util.ServiceLoader;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class RealmBuilder {
    
    public static RealmBuilder realmBuilder() {
        return new RealmBuilder();
    }
    
    public Realm build(String realmName, Map<String, String> properties) {
        ServiceLoader<RealmHandler> handlers = ServiceLoader.load(RealmHandler.class);
        for(RealmHandler handler : handlers) {
            if (handler.canHandle(realmName)) {
                Realm realm = handler.create(properties);
                realm.setName(realmName);
                
                return realm;
            }
        }
        
        throw new IllegalStateException("Unable to create realm " + realmName + 
                " : no handler found !");
    }
}
