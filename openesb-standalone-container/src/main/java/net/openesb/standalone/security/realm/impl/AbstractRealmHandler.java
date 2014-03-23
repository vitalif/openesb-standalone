package net.openesb.standalone.security.realm.impl;

import java.util.Map;
import net.openesb.standalone.security.realm.Realm;
import net.openesb.standalone.security.realm.RealmHandler;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public abstract class AbstractRealmHandler<T extends Realm> implements RealmHandler<T> {

    @Override
    public T create(String realmName, Map<String, String> properties) {
        return instantiate(realmName, properties);
    }
    
    abstract T instantiate(String realmName, Map<String, String> properties);
}
