package net.openesb.standalone.security.realm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public abstract class AbstractRealm implements Realm {
    
    private String realmName;
    
    protected AbstractRealm() {
    }
    
    protected AbstractRealm(String realmName) {
        this.realmName = realmName;
    }

    @Override
    public String getName() {
        return realmName;
    }

    public void setName(String realmName) {
        this.realmName = realmName;
    }
}
