package net.openesb.standalone.security.realm;

import java.util.Map;
import java.util.ServiceLoader;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.utils.I18NBundle;

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
        for (RealmHandler handler : handlers) {
            String type = properties.get(REALM_TYPE);
            if (handler.canHandle(type)) {
                Realm realm = handler.create(realmName, properties);

                return realm;
            }
        }

        String msg = I18NBundle.getBundle().getMessage(
                LocalStringKeys.SECURITY_REALM_HANDLER_NOT_FOUND, realmName);
                
        throw new IllegalStateException(msg);
    }
}
