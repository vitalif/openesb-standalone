package net.openesb.standalone.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.security.realm.Realm;
import net.openesb.standalone.security.realm.RealmBuilder;
import net.openesb.standalone.security.realm.shiro.ShiroAuthenticator;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SecurityProviderImpl implements SecurityProvider {

    private final Logger mLog =
            Logger.getLogger(this.getClass().getPackage().getName());
    
    private final Map<String, Realm> realms = new HashMap<String, Realm>();
    private final ShiroAuthenticator authenticator = new ShiroAuthenticator();
    private String adminRealmName = null;
    
    public SecurityProviderImpl(Map<String, Map<String, String>> realmsConfiguration) {
        this.init(realmsConfiguration);
        this.validate();
    }
    
    private void init(Map<String, Map<String, String>> realmsConfiguration) {
        if (realmsConfiguration != null) {
            mLog.log(Level.INFO, "Loading realms from configuration file.");
        
            for(Map.Entry<String, Map<String, String>> realmConfig : realmsConfiguration.entrySet()) {
                Realm realm = RealmBuilder.
                        realmBuilder().
                        build(realmConfig.getKey(), realmConfig.getValue());
                
                realms.put(realmConfig.getKey(), realm);
            }
        } else {
            mLog.log(Level.WARNING, "No realm defined !");
        }
    }
    
    private void validate() {
        for(Realm realm : realms.values()) {
            authenticator.loadRealm(realm);
            
            if (realm.isAdmin()) {
                if (adminRealmName == null) {
                    adminRealmName = realm.getName();
                } else {
                    throw new IllegalStateException(
                            "Admin realm already defined: " + adminRealmName);
                }
            }
        }
    }
    
    @Override
    public Collection<String> getRealms() {
        return Collections.unmodifiableSet(
                realms.keySet());
    }

    @Override
    public String getAdminRealm() {
        return adminRealmName;
    }

    @Override
    public boolean isAvailable(String realmName) {
        return realms.containsKey(realmName);
    }

    @Override
    public Subject login(String realmName, AuthenticationToken authenticationToken) throws AuthenticationException {
        return authenticator.authenticate(realmName, authenticationToken);
    }
}
