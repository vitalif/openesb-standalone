package net.openesb.standalone.rest.security.impl;

import java.io.Serializable;
import java.util.UUID;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionIdGenerator;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class JavaUuidSessionIdGenerator implements SessionIdGenerator {
    
    /**
     * Ignores the method argument and simply returns
     * {@code UUID}.{@link java.util.UUID#randomUUID() randomUUID()}.{@code toString()}.
     *
     * @param session the {@link Session} instance to which the ID will be applied.
     * @return the String value of the JDK's next {@link UUID#randomUUID() randomUUID()}.
     */
    @Override
    public Serializable generate(Session session) {
        return UUID.randomUUID().toString();
    }
}
