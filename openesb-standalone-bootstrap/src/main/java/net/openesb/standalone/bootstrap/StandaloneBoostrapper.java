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
 * @(#)JSEJBIBootstrap.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package net.openesb.standalone.bootstrap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class StandaloneBoostrapper 
        implements Runnable
{    
    /** JSR208 interfaces. */    
    private static final String JBI_JAR_NAME = "jbi.jar";
    
    /** JBI runtime interfaces exposed to components. */
    private static final String JBI_EXT_JAR_NAME = "jbi-ext.jar";
    
    /** Name of the top-level class of the JBI runtime framework. */
    private static final String JBI_FRAMEWORK_CLASS_NAME =
        "net.openesb.standalone.framework.JSEJBIFramework";
        
    /** Runtime life cycle commands. */
    private static final String START = "start";
    private static final String STOP = "stop";
    
    /** Environment property used to override install root location. */
    private static final String INSTALL_ROOT = "install.root";
    /** Environment property used for instance name. */
    public static final String INSTANCE_NAME = "instance.name";
    /** Environment property used for JMX connector port setting. */
    private static final String CONNECTOR_PORT = "connector.port";
    /** Default connector port. */
    private static final String DEFAULT_CONNECTOR_PORT = "8699";
    /** Default instance name. */
    private static final String DEFAULT_INSTANCE_NAME = "server";
    
    
    /** List of jars that should not be included in the runtime classloader. */
    private List<String> mBlacklistJars = new ArrayList<String>();
    
    /** ClassLoader used for JBI runtime classes.  These classes are not 
     *  part of the component classloader hierarchy.
     */
    private ClassLoader mFrameworkClassLoader;
    
    /** ClassLoader for clases in lib/ext that become part of the component
     *  classloader hierarchy.
     */
    private ClassLoader mExtensionClassLoader;
    
    /** JBI installation directory. */
    private File mJbiInstallDir;
    
    /** JBI Framework implementation */
    private Object mJbiFramework;
    
    /** Environment properties */
    private Properties mEnvironment;
    
    private Logger mLog = 
            Logger.getLogger(this.getClass().getPackage().getName());
    
    /** Runs the JBI framework in stand-alone mode under Java SE.  System
     *  properties defined at the command-line with '-D' are passed into the
     *  framework as environment properties.
     * @param args not used at this time
     * @throws Exception framework failed to initialize
     */
    public static void main(String[] args)
    {
        StandaloneBoostrapper jbiBootstrap = new StandaloneBoostrapper(System.getProperties());
        
        try
        {
            jbiBootstrap.createJBIFramework();
            
            // Are we starting or stopping?  Default to start.
            if (args != null && args.length > 0 && STOP.equalsIgnoreCase(args[0]))
            {
                jbiBootstrap.unloadJBIFramework();
            }
            else
            {
                jbiBootstrap.loadJBIFramework();
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }
    
    /** Create a new JSEJBIBootstrap instance with the specified environment.
     * @param env environment properties
     */
    public StandaloneBoostrapper(Properties env)
    {
        mEnvironment = env;
        
        // setup blacklist jars
        mBlacklistJars.add(JBI_JAR_NAME);
        mBlacklistJars.add(JBI_EXT_JAR_NAME);
        
        // If connector port is not specified, set a 'smart' value
        if (mEnvironment.getProperty(CONNECTOR_PORT) == null)
        {
            mEnvironment.setProperty(CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT);
        }
        
        // If install root is not set, default to current working directory
        String installPath = mEnvironment.getProperty(INSTALL_ROOT);
        if (installPath == null)
        {
            File installDir = new File(System.getProperty("user.dir"));
            // account for javaw launch from a double-click on the jar
            if (installDir.getName().equals("lib"))
            {
                installDir = installDir.getParentFile();
            }
            
            installPath = installDir.getAbsolutePath();
        }
        
        mJbiInstallDir = new File(installPath);
        
        // quick sanity check on the install root
        if (!mJbiInstallDir.isDirectory() ||
            !new File(mJbiInstallDir, "lib/jbi_rt.jar").exists())
        {
            throw new RuntimeException("Invalid JBI install root: " + 
                    mJbiInstallDir.getAbsolutePath());
        }
        
        // pass this information along to the core framework
        mEnvironment.setProperty(INSTALL_ROOT, mJbiInstallDir.getAbsolutePath());
    }
    
    /** Shutdown hook to allow the JBI framework to exit gracefully in the 
     *  event of an abrupt shutdown (e.g. ^C).
     */
    public void run()
    {
        try
        {
            // Using System.out because the loggers appear to be gone at this point
            System.out.println("Unloading JBI framework in response to VM termination.");
            invoke(mJbiFramework, "unload");
            System.out.println("JBI framework shutdown complete.");
        }
        catch (Throwable t)
        {
            mLog.log(Level.SEVERE, "Failed to unload JBI framework: {0}", t.toString());
        }
    }
    
    /** Loads the JBI framework using the Java SE platform wrapper.  If the
     *  framework loads successfully, this method adds a shutdown hook to 
     *  allow for civilized clean-up when the VM terminates.
     */
    void loadJBIFramework()
    {
        try
        {
            invoke(mJbiFramework, "load");
        
            //Add a shutdown hook to call unload when the VM exits
            Runtime.getRuntime().addShutdownHook(new Thread(this));
        }
        catch (Throwable t)
        {
            mLog.log(Level.SEVERE, "Failed to load JBI framework: {0}", t.toString());
        }
    }
    
    /** Unloads the JBI framework using the Java SE platform wrapper.  This is
     *  always a remote call, since the framework has (presumably) been loaded 
     *  previously by another process.
     */
    void unloadJBIFramework()
    {
        String          errMsg = null;
        JMXServiceURL   serviceURL;
        int             jmxConnectorPort;
        
        try
        {
            // Which port is the connector server running on?
            jmxConnectorPort = Integer.parseInt(mEnvironment.getProperty(
                        CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT));
            
            serviceURL = (JMXServiceURL)invoke(mJbiFramework, "getServiceURL", 
                    new Integer(jmxConnectorPort));
            
            JMXConnector jmxConn = JMXConnectorFactory.connect(serviceURL);
            MBeanServerConnection mbsConn = jmxConn.getMBeanServerConnection();
            ObjectName fwMBeanName = new ObjectName("com.sun.jbi.jse", "instance", 
                    mEnvironment.getProperty(INSTANCE_NAME, DEFAULT_INSTANCE_NAME));
            mbsConn.invoke(fwMBeanName, "unload", new Object[0], new String[0]);
            
        }    
        catch (NumberFormatException nfEx)
        {
            mLog.log(Level.SEVERE, "Invalid JMX connector port value.  {0}", nfEx.getMessage());
        }
        catch (javax.management.MBeanException mbEx)
        {
            errMsg = mbEx.getTargetException().toString();
        }
        catch (Throwable t)
        {
            errMsg = t.toString();
        }
        
        if (errMsg != null)
        {
            mLog.log(Level.SEVERE, "Failed to unload JBI framework: {0}", errMsg);
        }
        else
        {
            mLog.log(Level.INFO, "JBI framework has been unloaded.");
        }
    }
    
    /** Creates the JBI framework using the appropriate classloading structure.
     */
    private void createJBIFramework()
        throws Exception
    {
        Class       fwClass;
        Constructor fwCtor;
        
        try
        {
            createFrameworkClassLoader();
            createExtensionClassLoader();
            
            // Set the thread context classloader to the extension classloader
            Thread.currentThread().setContextClassLoader(mExtensionClassLoader);
            
            fwClass = mFrameworkClassLoader.loadClass(JBI_FRAMEWORK_CLASS_NAME);
            fwCtor = fwClass.getDeclaredConstructor(Properties.class);
            mJbiFramework = fwCtor.newInstance(mEnvironment);
        }
        catch (Exception ex)
        {
            throw new Exception("Failed to create JBI framework: " + ex.getMessage());
        }
    }
    
    /** Creates a separate runtime classloader to avoid namespace pollution
     *  between the component classloading hierarchy and the JBI implementation.
     *  At present, this method is greedy and includes any file in the lib/ 
     *  directory in the runtime classpath.
     */
    private void createFrameworkClassLoader()
    {
        ArrayList<URL> cpList = new ArrayList<URL>();
        URL[] cpURLs = new URL[0];
        File libDir = new File(mJbiInstallDir, "lib");        
        
        // Everything in the lib directory goes into the classpath
        for (File lib : libDir.listFiles())
        {
            try
            {
                if (mBlacklistJars.contains(lib.getName()))
                {
                    // skip blacklisted jars
                    continue;
                }
                
                cpList.add(lib.toURI().toURL());
            }
            catch (java.net.MalformedURLException urlEx)
            {
                mLog.log(Level.WARNING, "Bad library URL: {0}", urlEx.getMessage());
            }
        }
        
        cpURLs = cpList.toArray(cpURLs);
        mFrameworkClassLoader = new URLClassLoader(
                cpURLs, getClass().getClassLoader());
    }
    
    /** Creates a separate extension classloader for the component classloading
     *  chain.  All jars added in the lib/ext directory are automatically added
     *  to this classloader's classpath.
     */
    private void createExtensionClassLoader()
    {
        ArrayList<URL> cpList = new ArrayList<URL>();
        URL[] cpURLs = new URL[0];
        File libDir = new File(mJbiInstallDir, "lib/ext"); 
         
        if (libDir.exists() || libDir.isDirectory())
        {
            try
            {
                // Add the top-level ext directory
                cpList.add(libDir.toURI().toURL());

                // Everything in the lib/ext directory goes into the classpath
                for (File lib : libDir.listFiles())
                {
                    cpList.add(lib.toURI().toURL());
                }
            }
            catch (java.net.MalformedURLException urlEx)
            {
                mLog.log(Level.WARNING, "Bad library URL: {0}", urlEx.getMessage());
            }
        }

        cpURLs = cpList.toArray(cpURLs);
        mExtensionClassLoader = new URLClassLoader(
                cpURLs, mFrameworkClassLoader);
    }
    
    /** Utility method to invoke a method using reflection.  This is kind of
     *  a sloppy implementation, since we don't account for overloaded methods.
     *  @param obj contains the method to be invoked
     *  @param method name of the method to be invoked
     *  @param params parameters, if any
     *  @return returned object, if any
     */
    private Object invoke(Object obj, String method, Object... params)
        throws Throwable
    {
        Object result = null;
        
        try
        {
            for (Method m : obj.getClass().getDeclaredMethods())
            {
                if (m.getName().equals(method))
                {                    
                    result = m.invoke(obj, params);
                    break;
                }
            }
            
            return result;
        }
        catch (java.lang.reflect.InvocationTargetException itEx)
        {
            throw itEx.getTargetException();
        }
    }
}
