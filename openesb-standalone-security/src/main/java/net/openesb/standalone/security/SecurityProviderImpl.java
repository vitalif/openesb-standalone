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
    
    private final static String MANAGEMENT_REALM = "admin";
    
    private final Map<String, Realm> realms = new HashMap<String, Realm>();
    private final ShiroAuthenticator authenticator = new ShiroAuthenticator();
    
    public SecurityProviderImpl(Map<String, Map<String, String>> realmsConfiguration) {
        this.init(realmsConfiguration);
    }
    
    private void init(Map<String, Map<String, String>> realmsConfiguration) {
        if (realmsConfiguration != null) {
            mLog.log(Level.INFO, "Loading security realms from configuration.");
        
            for(Map.Entry<String, Map<String, String>> realmConfig : realmsConfiguration.entrySet()) {
                if (! realms.containsKey(realmConfig.getKey())) {
                    Realm realm = RealmBuilder.
                        realmBuilder().
                        build(realmConfig.getKey(), realmConfig.getValue());
                    
                    authenticator.loadRealm(realm);
                    realms.put(realmConfig.getKey(), realm);
                    
                    if (realm.getName().equals(MANAGEMENT_REALM)) {
                        mLog.log(Level.INFO, "Management Realm ({0}) has been correctly configured.",
                            realmConfig.getKey());
                    } else {
                        mLog.log(Level.INFO, "Realm {0} has been correctly configured.",
                            realmConfig.getKey());
                    }
                } else {
                    mLog.log(Level.INFO, "Realm {0} is already defined, skipping...",
                            realmConfig.getKey());
                }
            }
        } else {
            mLog.log(Level.WARNING, "No realm defined. Please have a look to "
                    + " the configuration !");
        }
    }
    
    @Override
    public Collection<String> getRealms() {
        return Collections.unmodifiableSet(
                realms.keySet());
    }

    @Override
    public Subject login(String realmName, AuthenticationToken authenticationToken) throws AuthenticationException {
        return authenticator.authenticate(realmName, authenticationToken);
    }
    
    @Override
    public Subject login(AuthenticationToken authenticationToken) throws AuthenticationException {
        return login(MANAGEMENT_REALM, authenticationToken);
    }
}
