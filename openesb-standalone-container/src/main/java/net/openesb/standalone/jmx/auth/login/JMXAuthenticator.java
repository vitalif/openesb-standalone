package net.openesb.standalone.jmx.auth.login;

import javax.inject.Inject;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.security.SecurityProvider;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class JMXAuthenticator implements javax.management.remote.JMXAuthenticator {

    @Inject
    private SecurityProvider securityProvider;
    
    @Override
    public Subject authenticate(Object credentialsObj) {
        final String [] credentials = (String []) credentialsObj;
        
        if (credentials == null || credentials.length != 2) {
            throw new SecurityException("Bad credentials. Unable to authenticate user.");
        }
        
        try {
            return securityProvider.login(new AuthenticationToken() {
                @Override
                public Object getPrincipal() {
                    return credentials[0];
                }

                @Override
                public Object getCredentials() {
                    return credentials[1].toCharArray();
                }
            });
        } catch (AuthenticationException ae) {
            throw new SecurityException(ae.getMessage());
        }
    }
}
