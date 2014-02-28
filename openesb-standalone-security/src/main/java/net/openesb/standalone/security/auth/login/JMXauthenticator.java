package net.openesb.standalone.security.auth.login;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.SecurityProvider;
import net.openesb.security.UsernamePasswordToken;

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
        String [] credentials = (String []) credentialsObj;
        String username = credentials[0];
        String password = credentials[1];
        
        try {
            return securityProvider.login("admin-realm", 
                new UsernamePasswordToken(username, password));
        } catch (AuthenticationException ae) {
            throw new SecurityException(ae.getMessage());
        }
    }
    
}
