package net.openesb.standalone.security.realm.shiro;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.standalone.security.realm.Realm;
import net.openesb.standalone.security.realm.impl.PropertiesRealm;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class ShiroAuthenticator {
    
    private final Logger mLog =
            Logger.getLogger(this.getClass().getPackage().getName());
    
    private final Map <String, org.apache.shiro.mgt.SecurityManager> securityManagers = 
            new HashMap<String, org.apache.shiro.mgt.SecurityManager>();
    
    public void loadRealm(Realm realm) {
        //TODO: find a way to automate the convertion
        org.apache.shiro.realm.Realm sRealm = new PropertiesRealmConverter().convert((PropertiesRealm)realm);
        
        DefaultSecurityManager manager = new DefaultSecurityManager(sRealm);
        securityManagers.put(realm.getName(), manager);
    }

    public Subject authenticate(String realmName, AuthenticationToken authenticationToken) 
            throws AuthenticationException {
        org.apache.shiro.mgt.SecurityManager securityManager = securityManagers.get(realmName);
        org.apache.shiro.subject.Subject currentUser = 
                new org.apache.shiro.subject.Subject.Builder(securityManager).buildSubject();
        
        UsernamePasswordToken token = new UsernamePasswordToken(
                (String) authenticationToken.getPrincipal(), 
                (char []) authenticationToken.getCredentials());
        
        try {
            currentUser.login(token);
            
            Subject subject = new Subject();
            return subject;
        } catch (org.apache.shiro.authc.AuthenticationException ae) {
            throw new AuthenticationException(ae.getMessage());
        }
    }
}