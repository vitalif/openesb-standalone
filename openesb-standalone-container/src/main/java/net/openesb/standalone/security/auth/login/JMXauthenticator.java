package net.openesb.standalone.security.auth.login;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.security.SecurityProvider;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class JMXauthenticator implements JMXAuthenticator {

    private final SecurityProvider securityProvider;
    
    public JMXauthenticator(final SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }
    
    @Override
    public Subject authenticate(Object credentialsObj) {
        final String [] credentials = (String []) credentialsObj;
        
        try {
            return securityProvider.login(new AuthenticationToken() {
                @Override
                public Object getPrincipal() {
                    return credentials[0];
                }

                @Override
                public Object getCredentials() {
                    return credentials[1];
                }
            });
        } catch (AuthenticationException ae) {
            throw new SecurityException(ae.getMessage());
        }
    }
}
