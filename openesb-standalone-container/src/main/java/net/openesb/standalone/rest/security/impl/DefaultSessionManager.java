package net.openesb.standalone.rest.security.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionIdGenerator;
import net.openesb.standalone.rest.security.SessionManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class DefaultSessionManager implements SessionManager {

    private ConcurrentMap<Serializable, Session> sessions = 
            new ConcurrentHashMap<Serializable, Session>();
    
    @Inject
    private SessionIdGenerator idGenerator;
    
    @Override
    public Session getSession(Serializable sessionKey) {
        Session session = sessions.get(sessionKey);
        
        if (session != null) {
            try {
                session.validate();
                session.access();
                
                return session;
            } catch (Exception ex) {
                sessions.remove(session.getId());
            }
        }
        
        return null;
    }
    
    @Override
    public Session create(Map<String, Object> sessionContext) {
        Session session = new Session();
        Serializable sessionId = idGenerator.generate(session);
        session.setId(sessionId);
        
        sessions.put(session.getId(), session);
        return session;
    }
}
