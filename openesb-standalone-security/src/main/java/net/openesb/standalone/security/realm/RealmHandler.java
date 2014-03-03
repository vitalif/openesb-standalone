package net.openesb.standalone.security.realm;

import java.util.Map;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface RealmHandler<T extends Realm> {

    boolean canHandle(String type);
    
    T create(String realmName, Map<String, String> properties);
}
