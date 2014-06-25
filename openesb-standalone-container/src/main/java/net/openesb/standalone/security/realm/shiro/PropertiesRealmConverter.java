package net.openesb.standalone.security.realm.shiro;

import java.security.KeyStoreException;
import net.openesb.standalone.security.realm.Realm;
import net.openesb.standalone.security.utils.PasswordManagement;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
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
        cRealm.setCredentialsMatcher(new SimpleCredentialsMatcher() {
            
            private final PasswordManagement manager = new PasswordManagement();
            
            @Override
            protected Object getCredentials(AuthenticationToken token) {
                char [] credentials = (char []) token.getCredentials();
                
                try {
                    return manager.encrypt(new String(credentials));
                } catch (KeyStoreException ke) {
                    return null;
                }
            }
        });
        
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
