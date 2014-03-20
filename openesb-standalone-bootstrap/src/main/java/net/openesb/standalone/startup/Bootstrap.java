package net.openesb.standalone.startup;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class Bootstrap {

    /**
     * JSR208 interfaces.
     */
    private static final String JBI_JAR_NAME = "jbi.jar";
    /**
     * JBI runtime interfaces exposed to components.
     */
    private static final String JBI_EXT_JAR_NAME = "jbi-ext.jar";
    /**
     * List of jars that should not be included in the runtime classloader.
     */
    private List<String> mBlacklistJars = new ArrayList<String>();

    {
        mBlacklistJars.add(JBI_JAR_NAME);
        mBlacklistJars.add(JBI_EXT_JAR_NAME);
    }
    /**
     * ClassLoader used for JBI runtime classes. These classes are not part of
     * the component classloader hierarchy.
     */
    private ClassLoader mFrameworkClassLoader;
    /**
     * ClassLoader for clases in lib/ext that become part of the component
     * classloader hierarchy.
     */
    private ClassLoader mExtensionClassLoader;
    private final static Logger mLog =
            Logger.getLogger(Bootstrap.class.getName());
    /**
     * Daemon reference
     */
    private Object openesbDaemon = null;
    /**
     * Daemon object used by main.
     */
    private static Bootstrap daemon = null;
    private static final String OPENESB_HOME_PROP = "openesb.home";

    public void init() throws Exception {
        // Set OpenESB path
        setOpenesbHome();

        initClassLoaders();

        // Set the thread context classloader to the framework classloader
        Thread.currentThread().setContextClassLoader(mFrameworkClassLoader);

        Class<?> fwClass = mFrameworkClassLoader.loadClass(
                "net.openesb.standalone.startup.Container");
        Object startupInstance = fwClass.newInstance();

        openesbDaemon = startupInstance;
    }

    private void initClassLoaders() {
        createExtensionClassLoader();
        createFrameworkClassLoader();
    }

    /**
     * Set the
     * <code>openesb.home</code> System property to the current working
     * directory if it has not been set.
     */
    private void setOpenesbHome() {
        String installPath = System.getProperty(OPENESB_HOME_PROP);
        if (installPath == null) {
            File installDir = new File(System.getProperty("user.dir"));
            // account for javaw launch from a double-click on the jar
            if (installDir.getName().equals("lib")) {
                installDir = installDir.getParentFile();
            }

            installPath = installDir.getAbsolutePath();
        }

        File openesbHomeDir = new File(installPath);

        // quick sanity check on the install root
        if (!openesbHomeDir.isDirectory()
                || !new File(openesbHomeDir, "lib/jbi_rt.jar").exists()) {
            throw new RuntimeException("Invalid JBI install root: "
                    + openesbHomeDir.getAbsolutePath());
        }

        // pass this information along to the core framework
        System.setProperty(OPENESB_HOME_PROP,
                openesbHomeDir.getAbsolutePath());
    }

    /**
     * Start the OpenESB Standalone daemon.
     */
    public void start() throws Exception {
        if (openesbDaemon == null) {
            init();
        }

        Method method = openesbDaemon.getClass().getMethod("start", (Class[]) null);
        method.invoke(openesbDaemon, (Object[]) null);
    }
    public static final String DEFAULT_INSTANCE_NAME = "server";
    public static final int DEFAULT_INSTANCE_PORT = 8699;
    public static final String DEFAULT_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi";

    /**
     * Stop the OpenESB Standalone daemon.
     */
    public void stop(String[] arguments)
            throws Exception {

        String errMsg = null;
        try {
            Map env = new HashMap();
            String[] creds = {"admin", "admin"};
            env.put(JMXConnector.CREDENTIALS, creds);

            JMXServiceURL serviceURL = new JMXServiceURL(
                    String.format(DEFAULT_SERVICE_URL, DEFAULT_INSTANCE_PORT));

            JMXConnector jmxConn = JMXConnectorFactory.connect(serviceURL, env);
            MBeanServerConnection mbsConn = jmxConn.getMBeanServerConnection();
            ObjectName fwMBeanName = new ObjectName("net.open-esb.standalone",
                    "instance", DEFAULT_INSTANCE_NAME);
            mbsConn.invoke(fwMBeanName, "stop", new Object[0], new String[0]);

        } catch (NumberFormatException nfEx) {
            mLog.log(Level.SEVERE, "Invalid JMX connector port value.  {0}", nfEx.getMessage());
        } catch (javax.management.MBeanException mbEx) {
            errMsg = mbEx.getTargetException().toString();
        } catch (Throwable t) {
            errMsg = t.toString();
        }

        if (errMsg != null) {
            mLog.log(Level.SEVERE, "Failed to unload JBI framework: {0}", errMsg);
        } else {
            mLog.log(Level.INFO, "JBI framework has been unloaded.");
        }
    }

    /**
     * Main method, used for testing only.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        if (daemon == null) {
            // Don't set daemon until init() has completed
            Bootstrap bootstrap = new Bootstrap();
            /*
             try {
             bootstrap.init();
             } catch (Throwable t) {
             t.printStackTrace();
             return;
             }*/
            daemon = bootstrap;
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("start")) {
                daemon.start();
            } else if (command.equals("stop")) {
                daemon.stop(args);
            } else {
                mLog.log(Level.WARNING,
                        "Bootstrap: command \"{0}\" does not exist.", command);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates a separate runtime classloader to avoid namespace pollution
     * between the component classloading hierarchy and the JBI implementation.
     * At present, this method is greedy and includes any file in the lib/
     * directory in the runtime classpath.
     */
    private void createFrameworkClassLoader() {
        ArrayList<URL> cpList = new ArrayList<URL>();
        URL[] cpURLs = new URL[0];
        File libDir = new File(
                System.getProperty(OPENESB_HOME_PROP), "lib");

        // Everything in the lib directory goes into the classpath
        for (File lib : libDir.listFiles()) {
            try {
                if (mBlacklistJars.contains(lib.getName())) {
                    // skip blacklisted jars
                    continue;
                }

                mLog.log(Level.FINEST, "Framework classloader : loading library {0}", lib.getName());
                cpList.add(lib.toURI().toURL());
            } catch (java.net.MalformedURLException urlEx) {
                mLog.log(Level.WARNING, "Bad library URL: {0}", urlEx.getMessage());
            }
        }

        cpURLs = cpList.toArray(cpURLs);
        mFrameworkClassLoader = new URLClassLoader(
                cpURLs, mExtensionClassLoader);
    }

    /**
     * Creates a separate extension classloader for the component classloading
     * chain. All jars added in the lib/ext directory are automatically added to
     * this classloader's classpath.
     */
    private void createExtensionClassLoader() {
        ArrayList<URL> cpList = new ArrayList<URL>();
        URL[] cpURLs = new URL[0];
        File libDir = new File(
                System.getProperty(OPENESB_HOME_PROP), "lib/ext");

        if (libDir.exists() || libDir.isDirectory()) {
            try {
                // Add the top-level ext directory
                cpList.add(libDir.toURI().toURL());

                // Everything in the lib/ext directory goes into the classpath
                for (File lib : libDir.listFiles()) {
                    mLog.log(Level.FINEST, "Extension classloader : loading library {0}", lib.getName());
                    cpList.add(lib.toURI().toURL());
                }
            } catch (java.net.MalformedURLException urlEx) {
                mLog.log(Level.WARNING, "Bad library URL: {0}", urlEx.getMessage());
            }
        }

        cpURLs = cpList.toArray(cpURLs);
        mExtensionClassLoader = new URLClassLoader(
                cpURLs, getClass().getClassLoader());
    }
}
