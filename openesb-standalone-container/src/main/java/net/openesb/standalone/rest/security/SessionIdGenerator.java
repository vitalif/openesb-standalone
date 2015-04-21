package net.openesb.standalone.rest.security;

import java.io.Serializable;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface SessionIdGenerator {
    
    Serializable generate(Session session);
}
