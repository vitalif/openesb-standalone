package net.openesb.standalone.security.realm.shiro;

import net.openesb.standalone.security.realm.Realm;
import org.apache.shiro.realm.text.PropertiesRealm;


/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PropertiesRealmConverter implements 
        RealmConverter<net.openesb.standalone.security.realm.impl.PropertiesRealm, PropertiesRealm> {

    @Override
    public PropertiesRealm convert(net.openesb.standalone.security.realm.impl.PropertiesRealm realm) {
        PropertiesRealm cRealm = new PropertiesRealm();
        cRealm.setResourcePath(realm.getPath());
        
        if (realm.isReload()) {
            cRealm.setReloadIntervalSeconds(realm.getReloadInterval());
        }
        
        // Initialize the realm
        cRealm.onInit();
        
        return cRealm;
    }

    @Override
    public boolean canHandle(Class<? extends Realm> realm) {
        return realm.equals(net.openesb.standalone.security.realm.impl.PropertiesRealm.class);
    }

}
