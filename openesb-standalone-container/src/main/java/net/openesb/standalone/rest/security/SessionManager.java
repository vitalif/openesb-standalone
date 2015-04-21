package net.openesb.standalone.rest.security;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface SessionManager {
    
    Session getSession(Serializable sessionKey);
    
    Session create(Map<String, Object> sessionContext);
}
