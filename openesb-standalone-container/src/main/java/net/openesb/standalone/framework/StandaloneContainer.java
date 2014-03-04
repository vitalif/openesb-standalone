package net.openesb.standalone.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import net.openesb.standalone.jmx.MBServerConnectorFactory;
import net.openesb.standalone.security.SecurityProviderImpl;
//import net.openesb.standalone.node.Node;
//import net.openesb.standalone.node.NodeBuilder;
import net.openesb.standalone.settings.ImmutableSettings;
import net.openesb.standalone.settings.Settings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

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

    public static final String CONFIG_FILE = "openesb.config";
    public static final String INSTALL_ROOT = "install.root";
    public static final String INSTANCE_NAME = "instance.name";
    public static final String CONNECTOR_PORT = "instance.port";
    /**
     * Configuration defaults.
     */
    private static final String DEFAULT_INSTALL_ROOT = System.getProperty("user.dir");
    private static final String DEFAULT_INSTANCE_NAME = "server";
    private static final int DEFAULT_CONNECTOR_PORT = 8699;
    private StandalonePlatformContext mPlatformContext;
    private boolean mLoaded;
    private final Properties systemEnv;
    
    private static final Logger mLog =
            Logger.getLogger(StandaloneContainer.class.getPackage().getName());
//    private Node instanceNode;
    private Settings settings;

    /**
     * Creates a new instance of the JBI framework.
     */
    public StandaloneContainer(Properties environment) {
        super();
        systemEnv = environment;
    }

    private void init() throws Exception {
        String installRoot = systemEnv.getProperty(INSTALL_ROOT, DEFAULT_INSTALL_ROOT);

        String configFile = systemEnv.getProperty(CONFIG_FILE);

        if (configFile == null) {
            configFile = installRoot + File.separatorChar + "config/openesb.yaml";
        }

        mLog.log(Level.FINE, "Trying to load configuration from {0}", configFile);

        Map configurations = null;
        
        try {
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(),
                    new Resolver() {
                @Override
                protected void addImplicitResolvers() {
                }
            });
            InputStream input = new FileInputStream(new File(configFile));
            configurations = (Map) yaml.load(input);
            
            settings = new ImmutableSettings(configurations);
            mLog.log(Level.FINE, "Configuration loaded from {0}", configFile);
        } catch (FileNotFoundException fnfe) {
            mLog.log(Level.WARNING, "Unable to load configuration file {0}. Default configuration will be used.", configFile);
            settings = new ImmutableSettings(null);
        }

        mPlatformContext = new StandalonePlatformContext(
                installRoot,
                settings.get(INSTANCE_NAME, DEFAULT_INSTANCE_NAME),
                settings.getAsInt(CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT));
        
        mPlatformContext.setSecurityProvider(
                new SecurityProviderImpl(
                (Map<String,Map<String,String>>) configurations.get("realm")));
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
            throw new IllegalStateException("JBI framework already loaded!");
        }

        this.init();

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

        // Setup the remote JMX connector server
        Integer port = null;

        try {
            port = settings.getAsInt(CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT);
            MBServerConnectorFactory.getInstance().setPort(port);
            MBServerConnectorFactory.getInstance().setMBeanServer(
                    mPlatformContext.getMBeanServer());
            
            MBServerConnectorFactory.getInstance().createConnector();
        } catch (NumberFormatException nfEx) {
            mLog.log(Level.WARNING, "Invalid connector server port: {0}. Remote JMX connector will not be created.", port);
        }

        // For stand-alone JBI, JBI_HOME = platform install root
        systemEnv.setProperty("com.sun.jbi.home",
                mPlatformContext.getInstallRoot());

        // --------------------------------------------
        // TODO: removing this part asap
        System.setProperty("http.port",
                settings.get("http.port", "4848"));
        System.setProperty("http.enabled",
                settings.get("http.enabled", "true"));
        // --------------------------------------------

        init(mPlatformContext, systemEnv);
        startup(mPlatformContext.getNamingContext(), "");
        prepare();
        ready(true);

        //    instanceNode = NodeBuilder.nodeBuilder(settings).build();

        //    instanceNode.start();

        // JBI framework has been loaded
        mLoaded = true;
    }
    
    public String getServiceUrl() {
        return MBServerConnectorFactory.getInstance().getServiceUrl();
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

        //    instanceNode.stop();

        shutdown();
        terminate();

        try {
            MBServerConnectorFactory.getInstance().destroy();
        } catch (Exception ex) {
            mLog.log(Level.SEVERE, "Error during framework shutdown: {0}", ex.toString());
        }

        mLoaded = false;
    }
}
