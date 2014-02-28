package net.openesb.standalone.security.realm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public abstract class AbstractRealm implements Realm {
    
    private String realmName;
    private boolean admin = false;
    
    protected AbstractRealm() {
    }
    
    protected AbstractRealm(String realmName) {
        this.realmName = realmName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getName() {
        return realmName;
    }

    public void setName(String realmName) {
        this.realmName = realmName;
    }
}
