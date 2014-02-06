/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)JSEJBIFramework.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package net.openesb.standalone.framework;

import com.sun.jndi.rmi.registry.RegistryContextFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
//import net.openesb.standalone.node.Node;
//import net.openesb.standalone.node.NodeBuilder;
import net.openesb.standalone.settings.ImmutableSettings;
import net.openesb.standalone.settings.Settings;
import org.yaml.snakeyaml.Yaml;

/**
 * JBI framework wrapper for Java SE platform.
 * <br><br>
 * A JSEJBIFramework instance cannot be loaded multiple times in the same VM. If
 * multiple instances of the framework are required in a VM, instantiate
 * multiple instances of JSEJBIFramework and load each one independently. There
 * is no limit on the number of uniquely named JSEJBIFramework instances in the
 * same VM. A specific JSEJBIFramework instance can be loaded and unloaded
 * multiple times in a VM.
 *
 * @author Sun Microsystems, Inc.
 */
public class JSEJBIFramework
        extends com.sun.jbi.framework.JBIFramework
        implements JSEJBIFrameworkMBean {

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
    private JSEPlatformContext mPlatformContext;
    private boolean mLoaded;
    private Properties mEnvironment;
    private JMXConnectorServer mJMXServer;
    private Registry mRegistry;
    private Logger mLog =
            Logger.getLogger(this.getClass().getPackage().getName());
//    private Node instanceNode;
    private Settings settings;

    /**
     * Creates a new instance of the JBI framework.
     */
    public JSEJBIFramework(Properties environment) {
        super();

        mEnvironment = environment;
    }

    private void init() throws Exception {
        String installRoot = mEnvironment.getProperty(INSTALL_ROOT, DEFAULT_INSTALL_ROOT);

        String configFile = mEnvironment.getProperty(CONFIG_FILE);

        if (configFile == null) {
            configFile = installRoot + File.separatorChar + "config/openesb.yaml";
        }

        mLog.log(Level.FINE, "Trying to load configuration from {0}", configFile);

        try {
            Yaml yaml = new Yaml();
            InputStream input = new FileInputStream(new File(configFile));
            settings = new ImmutableSettings((Map) yaml.load(input));
            mLog.log(Level.FINE, "Configuration loaded from {0}", configFile);
        } catch (FileNotFoundException fnfe) {
            mLog.log(Level.WARNING, "Unable to load configuration file {0}. Default configuration will be used.", configFile);
            settings = new ImmutableSettings(null);
        }
        
        mPlatformContext = new JSEPlatformContext(
                    installRoot,
                    settings.get(INSTANCE_NAME, DEFAULT_INSTANCE_NAME),
                    settings.getAsInt(CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT));
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
        ObjectName fwMBeanName = new ObjectName("com.sun.jbi.jse",
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
            createJMXConnectorServer(port);
        } catch (NumberFormatException nfEx) {
            mLog.log(Level.WARNING, "Invalid connector server port: {0}. Remote JMX connector will not be created.", port);
        }

        // For stand-alone JBI, JBI_HOME = platform install root
        mEnvironment.setProperty("com.sun.jbi.home",
                mPlatformContext.getInstallRoot());

        // --------------------------------------------
        // TODO: removing this part asap
        System.setProperty("http.port", 
                settings.get("http.port", "4848"));
        System.setProperty("http.enabled", 
                settings.get("http.enabled", "true"));
        // --------------------------------------------
        
        init(mPlatformContext, mEnvironment);
        startup(mPlatformContext.getNamingContext(), "");
        prepare();
        ready(true);

    //    instanceNode = NodeBuilder.nodeBuilder(settings).build();

    //    instanceNode.start();

        // JBI framework has been loaded
        mLoaded = true;
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
            mJMXServer.stop();
            UnicastRemoteObject.unexportObject(mRegistry, true);
        } catch (Exception ex) {
            mLog.log(Level.SEVERE, "Error during framework shutdown: {0}", ex.toString());
        }

        mLoaded = false;
    }

    private JMXServiceURL getServiceURL(int port)
            throws java.net.MalformedURLException {
        return new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");
    }

    /**
     * Creates a JMX connector server at the specified port.
     *
     * @param port port for the JMX connector server.
     */
    private void createJMXConnectorServer(int port) {
        HashMap<String, String> map = new HashMap<String, String>();

        /*
         map.put("java.naming.factory.initial", 
         RegistryContextFactory.class.getName());
         */
        try {
            // Create the service URL
            JMXServiceURL serviceURL = getServiceURL(port);

            // Create an RMI registry instance to hold the JMX connector server
            mRegistry = LocateRegistry.createRegistry(port);

            // Create and start the connector server
            mJMXServer = JMXConnectorServerFactory.newJMXConnectorServer(
                    serviceURL, map, mPlatformContext.getMBeanServer());
            mJMXServer.start();

            mLog.log(Level.INFO, "remote JMX connector available at {0}", mJMXServer.getAddress());
        } catch (Exception ex) {
            mLog.log(Level.SEVERE, "Failed to create remote JMX connector: {0}", ex.toString());
        }
    }
}
