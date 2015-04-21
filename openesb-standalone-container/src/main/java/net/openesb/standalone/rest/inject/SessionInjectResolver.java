package net.openesb.standalone.rest.inject;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionManager;
import org.glassfish.hk2.api.Factory;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SessionInjectResolver implements Factory<Session> {

    private final HttpHeaders request;
    private final SessionManager sessionManager;
    
    @Inject
    public SessionInjectResolver(HttpHeaders request, SessionManager sessionManager) {
        this.request = request;
        this.sessionManager = sessionManager;
    }
    
    @Override
    public Session provide() {
        String authHeader = request.getHeaderString("authorization");
        
        if (authHeader != null) {
            String token = decode(authHeader);

            return sessionManager.getSession(token);
        }
        
        return null;
    }

    @Override
    public void dispose(Session t) {
    }
    
    private static String decode(String auth) {
        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
        return auth.replaceFirst("[T|t]oken ", "");
    }
}