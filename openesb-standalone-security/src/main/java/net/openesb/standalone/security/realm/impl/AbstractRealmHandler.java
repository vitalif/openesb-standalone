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
    
    /**
     * System property replacement in the given string.
     *
     * @param str The original string
     * @return the modified string
     */
    protected String replace(String str) {
        String result = str;
        int pos_start = str.indexOf("${");
        if (pos_start >= 0) {
            StringBuilder builder = new StringBuilder();
            int pos_end = -1;
            while (pos_start >= 0) {
                builder.append(str, pos_end + 1, pos_start);
                pos_end = str.indexOf('}', pos_start + 2);
                if (pos_end < 0) {
                    pos_end = pos_start - 1;
                    break;
                }
                String propName = str.substring(pos_start + 2, pos_end);
                String replacement = propName.length() > 0 ? System
                        .getProperty(propName) : null;
                if (replacement != null) {
                    builder.append(replacement);
                } else {
                    builder.append(str, pos_start, pos_end + 1);
                }
                pos_start = str.indexOf("${", pos_end + 1);
            }
            builder.append(str, pos_end + 1, str.length());
            result = builder.toString();
        }
        return result;
    }
}
