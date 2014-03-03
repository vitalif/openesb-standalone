package net.openesb.standalone.security.realm.impl;

import net.openesb.standalone.security.realm.AbstractRealm;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PropertiesRealm extends AbstractRealm {
    
    private String path;
    
    private boolean reload = false;
    
    /**
     * Unit: seconds
     */
    private int reloadInterval;

    public PropertiesRealm(String realmName) {
        super(realmName);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public int getReloadInterval() {
        return reloadInterval;
    }

    public void setReloadInterval(int reloadInterval) {
        this.reloadInterval = reloadInterval;
    }
}
