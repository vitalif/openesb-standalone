package net.openesb.standalone.framework;

import com.sun.jbi.platform.PlatformContext;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import net.openesb.standalone.inject.Injector;
import net.openesb.standalone.jmx.MBServerConnectorFactory;
import net.openesb.standalone.settings.Settings;

/**
 * JBI framework wrapper for OpenESB Standalone platform.
 * <br>
 * A JSEJBIFramework instance cannot be loaded multiple times in the same VM. If
 * multiple instances of the framework are required in a VM, instantiate
 * multiple instances of JSEJBIFramework and load each one independently. There
 * is no limit on the number of uniquely named JSEJBIFramework instances in the
 * same VM. A specific JSEJBIFramework instance can be loaded and unloaded
 * multiple times in a VM.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class StandaloneContainer
        extends com.sun.jbi.framework.JBIFramework
        implements StandaloneContainerMBean {

    private boolean mLoaded;
    public static Properties systemEnv;
    
    private static final Logger LOG =
            Logger.getLogger(StandaloneContainer.class.getPackage().getName());


    /**
     * Creates a new instance of the JBI framework.
     */
    public StandaloneContainer(Properties environment) {
        super();
        systemEnv = environment;
    }

    /**
     * Load the JBI framework with the specified environment. When this method
     * retuns, all public interfaces and system services have completely
     * initialized. If a connector port is specified in the environment
     * properties, a remote JMX connector server is created.
     *
     * @throws Exception failed to load JBI framework
     */
    @Override
    public synchronized void load()
            throws Exception {
        if (mLoaded) {
            throw new IllegalStateException("OpenESB runtime already loaded!");
        }

        PlatformContext mPlatformContext = Injector.getInstance().getInjector().getInstance(PlatformContext.class);

        // Register a management MBean for this framework instance
        ObjectName fwMBeanName = new ObjectName("net.open-esb.standalone",
                "instance", mPlatformContext.getInstanceName());
        MBeanServer mbs = mPlatformContext.getMBeanServer();
        if (mbs.isRegistered(fwMBeanName)) {
            if (mbs.getAttribute(fwMBeanName, "Loaded").equals(Boolean.TRUE)) {
                // Framework already loaded from a separate thread/process
                throw new IllegalStateException("JBI framework instance "
                        + mPlatformContext.getInstanceName() + " has already been loaded");
            } else {
                // MBean should not be registered - try to clean up
                mbs.unregisterMBean(fwMBeanName);
            }
        }
        mbs.registerMBean(this, fwMBeanName);

        MBServerConnectorFactory connectorFactory =
                Injector.getInstance().getInjector().getInstance(MBServerConnectorFactory.class);
        connectorFactory.createConnector();

        // For stand-alone JBI, JBI_HOME = platform install root
        systemEnv.setProperty("com.sun.jbi.home",
                mPlatformContext.getInstallRoot());

        // --------------------------------------------
        // TODO: removing this part asap
        Settings settings = Injector.getInstance().getInjector().getInstance(Settings.class);
        
        System.setProperty("http.port",
                settings.get("http.port", "4848"));
        System.setProperty("http.enabled",
                settings.get("http.enabled", "true"));
        // --------------------------------------------

        init(mPlatformContext, systemEnv);
        startup(mPlatformContext.getNamingContext(), "");
        prepare();
        ready(true);

        // JBI framework has been loaded
        mLoaded = true;
    }

    public String getServiceUrl() {
        MBServerConnectorFactory connectorFactory =
                Injector.getInstance().getInjector().getInstance(MBServerConnectorFactory.class);
        return connectorFactory.getServiceUrl();
    }

    /**
     * Queries the state of the JBI Framework.
     *
     * @return true if the JBI framework is loaded, false otherwise.
     */
    @Override
    public boolean isLoaded() {
        return mLoaded;
    }

    /**
     * Unloads the JBI framework. When this method retuns, all public
     * interfaces, system services, and JMX connector (if configured) have been
     * destroyed.
     *
     * @throws javax.jbi.JBIException failed to unload JBI framework
     */
    @Override
    public void unload()
            throws Exception {
        if (!mLoaded) {
            return;
        }

        shutdown();
        terminate();

        try {
            MBServerConnectorFactory connectorFactory =
                Injector.getInstance().getInjector().getInstance(MBServerConnectorFactory.class);
            connectorFactory.destroy();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error during framework shutdown: {0}", ex.toString());
        }

        mLoaded = false;
    }
}
