package net.openesb.standalone.security.realm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface Realm {
    
    void setName(String name);
    
    String getName();
    
    boolean isAdmin();
    
    void setAdmin(boolean isAdmin);
}
