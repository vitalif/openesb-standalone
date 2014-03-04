package net.openesb.standalone.framework;

/**
 *  Management interface for OpenESB Standalone Container.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface StandaloneContainerMBean 
{
    /** Queries the state of the JBI Framework.
     *  @return true if the JBI framework is loaded, false otherwise.
     */
    boolean isLoaded();
    
    /** Load the JBI framework with the specified environment.  When this method
     *  retuns, all public interfaces and system services have completely 
     *  initialized.  If a connector port is specified in the environment 
     *  properties, a remote JMX connector server is created.
     *  @throws Exception failed to load JBI framework
     */
    void load() throws Exception;
    
    /** Unloads the JBI framework.  When this method retuns, all 
     *  public interfaces, system services, and JMX connector (if configured)
     *  have been destroyed.
     *  @throws javax.jbi.JBIException failed to unload JBI framework
     */
    void unload() throws Exception;
}
