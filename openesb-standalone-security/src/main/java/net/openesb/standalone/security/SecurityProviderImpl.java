package net.openesb.standalone.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import net.openesb.security.AuthenticationException;
import net.openesb.security.AuthenticationToken;
import net.openesb.security.SecurityProvider;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.text.PropertiesRealm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SecurityProviderImpl implements SecurityProvider {

    private Logger mLog =
            Logger.getLogger(this.getClass().getPackage().getName());
    
    private final Map <String, org.apache.shiro.mgt.SecurityManager> securityManagers = 
            new HashMap<String, org.apache.shiro.mgt.SecurityManager>();
    
    public SecurityProviderImpl() {
        this.init();
    }
    
    private void init() {
        mLog.log(Level.INFO, "Loading Realms from configuration.");
        
        PropertiesRealm propertiesRealm = new PropertiesRealm();
        propertiesRealm.setResourcePath("/Users/david/test.properties");
        propertiesRealm.init();
        
        securityManagers.put("admin-realm", new DefaultSecurityManager(propertiesRealm));
    }
    
    @Override
    public Collection<String> getRealms() {
        return Collections.unmodifiableSet(
                securityManagers.keySet());
    }

    @Override
    public String getAdminRealm() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAvailable(String realmName) {
        return securityManagers.containsKey(realmName);
    }

    @Override
    public Subject login(String realmName, AuthenticationToken authenticationToken) throws AuthenticationException {
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
