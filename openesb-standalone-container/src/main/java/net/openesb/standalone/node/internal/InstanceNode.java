package net.openesb.standalone.node.internal;

import com.google.inject.Injector;
import com.sun.jbi.platform.PlatformContext;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import net.openesb.standalone.core.CoreModule;
import net.openesb.standalone.framework.FrameworkModule;
import net.openesb.standalone.framework.FrameworkService;
import net.openesb.standalone.inject.ModulesBuilder;
import net.openesb.standalone.jmx.JMXService;
import net.openesb.standalone.naming.NamingModule;
import net.openesb.standalone.node.Node;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class InstanceNode implements Node {

    private static final Logger LOG =
            Logger.getLogger(InstanceNode.class.getPackage().getName());
    
    private final Injector injector;
    
    private JMXService jmxService;
    private FrameworkService frameworkService;
    
    public InstanceNode () {
        LOG.info("Initializing new instance...");
        
        ModulesBuilder modules = new ModulesBuilder();
        
        modules.add(new CoreModule());
        modules.add(new FrameworkModule());
        modules.add(new NamingModule());
        modules.add(new NodeModule(this));
        
        injector = modules.createInjector();
        
        LOG.info("Instance initialized");
    }
    
    @Override
    public void start() {
        LOG.info("Instance is starting ...");
        
        jmxService = injector.getInstance(JMXService.class);
        jmxService.start();
        
        frameworkService = injector.getInstance(FrameworkService.class);
        frameworkService.start();
        
        PlatformContext platformContext = injector.getInstance(PlatformContext.class);
        
        try {
            // Register a management MBean for this framework instance
            ObjectName fwMBeanName = new ObjectName("net.open-esb.standalone",
                    "instance", platformContext.getInstanceName());
            MBeanServer mbs = platformContext.getMBeanServer();
            if (mbs.isRegistered(fwMBeanName)) {
                if (mbs.getAttribute(fwMBeanName, "Loaded").equals(Boolean.TRUE)) {
                    // Framework already loaded from a separate thread/process
                    throw new IllegalStateException("JBI framework instance "
                            + platformContext.getInstanceName() + " has already been loaded");
                } else {
                    // MBean should not be registered - try to clean up
                    mbs.unregisterMBean(fwMBeanName);
                }
            }
            
            final StandardMBean mbean = new StandardMBean(this, Node.class);
            mbs.registerMBean(mbean, fwMBeanName);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        LOG.info("Instance started");
    }

    @Override
    public void stop() {
        LOG.info("Instance is stopping ...");
        
        frameworkService.stop();
        jmxService.stop();
        
        LOG.info("Instance stopped");
    }
}
