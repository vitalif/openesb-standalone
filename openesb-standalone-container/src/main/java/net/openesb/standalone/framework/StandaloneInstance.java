package net.openesb.standalone.framework;

import javax.inject.Inject;
import net.openesb.standalone.jmx.MBServerConnectorFactory;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class StandaloneInstance implements StandaloneContainerMBean {

    private boolean mLoaded;
    
    @Inject
    private MBServerConnectorFactory connectorFactory;
    
    @Override
    public boolean isLoaded() {
        return mLoaded;
    }

    @Override
    public void load() throws Exception {
        connectorFactory.createConnector();
    }

    @Override
    public void unload() throws Exception {
        connectorFactory.destroy();
    }
    
}
