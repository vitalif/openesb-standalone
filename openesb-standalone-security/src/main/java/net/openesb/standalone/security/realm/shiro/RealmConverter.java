package net.openesb.standalone.security.realm.shiro;

import net.openesb.standalone.security.realm.Realm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface RealmConverter<T extends Realm, S extends org.apache.shiro.realm.Realm> {
    
    S convert(T realm);
}
