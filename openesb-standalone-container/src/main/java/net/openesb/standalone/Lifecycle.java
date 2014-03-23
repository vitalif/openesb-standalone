package net.openesb.standalone;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface Lifecycle {
    
    void start() throws LifecycleException;
    
    void stop() throws LifecycleException;
}
