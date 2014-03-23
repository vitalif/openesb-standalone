package net.openesb.standalone.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.security.realm.Realm;
import net.openesb.standalone.security.realm.RealmBuilder;
import net.openesb.standalone.security.realm.shiro.ShiroAuthenticator;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SecurityProviderImpl implements SecurityProvider {

    private static final Logger LOG =
            Logger.getLogger(SecurityProviderImpl.class.getPackage().getName());
    private final static String SETTINGS_KEY = "realm";
    private final static String MANAGEMENT_REALM = "admin";
    private final Map<String, Realm> realms = new HashMap<String, Realm>();
    private final ShiroAuthenticator authenticator = new ShiroAuthenticator();

    @Inject
    public SecurityProviderImpl(final Settings settings) {
        init(settings);
    }

    private void init(final Settings settings) {
        try {
            Map<String, Map<String, String>> realmsConfiguration =
                    (Map<String, Map<String, String>>) settings.getAsObject(SETTINGS_KEY);

            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.SECURITY_LOAD_CONFIGURATION));
            }

            for (Map.Entry<String, Map<String, String>> realmConfig : realmsConfiguration.entrySet()) {
                String realmName = realmConfig.getKey();
                
                if (! realms.containsKey(realmName)) {
                    Realm realm = RealmBuilder.
                            realmBuilder().
                            build(realmName, realmConfig.getValue());

                    authenticator.loadRealm(realm);
                    realms.put(realmName, realm);

                    if (LOG.isLoggable(Level.INFO)) {
                        if (realm.getName().equals(MANAGEMENT_REALM)) {
                            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                                    LocalStringKeys.SECURITY_ADMIN_REALM_CONFIGURED, realmName));
                        } else {
                            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                                    LocalStringKeys.SECURITY_USER_REALM_CONFIGURED, realmName));
                        }
                    }
                } else {
                    LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.SECURITY_USER_REALM_ALREADY_DEFINED, realmName));
                }
            }
        } catch (NullPointerException npe) {
            LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.SECURITY_NO_REALM));
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
