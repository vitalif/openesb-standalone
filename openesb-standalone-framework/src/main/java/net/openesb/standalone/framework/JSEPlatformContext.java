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
 * @(#)JSEPlatformContext.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package net.openesb.standalone.framework;

import com.sun.jbi.JBIProvider;
import com.sun.jbi.platform.PlatformEventListener;
import com.sun.jbi.security.KeyStoreUtil;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

/**
 * Implementation of PlatformContext for Java SE.
 * @author Sun Microsystems Inc.
 */
public class JSEPlatformContext implements com.sun.jbi.platform.PlatformContext
{
    private String          mInstanceName;
    private String          mInstanceRoot;
    private String          mInstallRoot;
    private String          mConnectorPort;
    private InitialContext  mNamingContext;
    private Logger          mLog = Logger.getLogger(getClass().getPackage().getName());
    
    public JSEPlatformContext(String instanceName, String installRoot, String connectorPort)
    {
        mInstanceName = instanceName;
        mInstallRoot  = installRoot;
        mInstanceRoot = installRoot + File.separator + instanceName;
        mConnectorPort = connectorPort;
        
        try
        {
            mNamingContext = new InitialContext();
        }
        catch (javax.naming.NamingException nmEx)
        {
            mLog.warning(nmEx.toString());
        }
    }
    
    /**
     * Get the TransactionManager for this implementation. The instance
     * returned is an implementation of the standard JTS interface. If none
     * is available, returns <CODE>null</CODE>.
     * @return a <CODE>TransactionManager</CODE> instance.
     * @throws Exception if a <CODE>TransactionManager</CODE> cannot be obtained.
     */
    public javax.transaction.TransactionManager getTransactionManager()
        throws Exception
    {
        return new com.atomikos.icatch.jta.UserTransactionManager();
    }

    /**
     * Get the MBean server connection for a particular instance.
     * @return the <CODE>MBeanServerConnection</CODE> for the specified instance.
     */
    public MBeanServerConnection getMBeanServerConnection(String instanceName)
        throws Exception
    {
        return java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }
    
    /**
     * Get the instance name of the platform's administration server.  If the
     * platform does not provide a separate administration server, then this 
     * method returns the name of the local instance.
     * @return instance name of the administration server
     */
    public String getAdminServerName()
    {
        return mInstanceName;
    }
   
    /**
     * Determine whether this instance is the administration server instance.
     * @return <CODE>true</CODE> if this instance is the administration server,
     * <CODE>false</CODE> if not.
     */
    public boolean isAdminServer()
    {
        return true;
    }
    
    /**
     * Get the name of this instance.
     * @return the name of this server instance.
     */
    public String getInstanceName()
    {
        return mInstanceName;
    }
        
    /**
     * Determine if the specified instance is up.
     * @return true if the instance is up and running, false otherwise
     */
    public boolean isInstanceUp(String instanceName)
    {
        return mInstanceName.equals(instanceName);
    }
    
    /**
     * Determine whether multiple servers are permitted within this AS
     * installation.
     * @return true if multiple servers are permitted.
     */
    public boolean supportsMultipleServers()
    {
        return false;
    }
    
    /**
     * Get the Target Name. If the instance is not a clustered instance then
     * the target name is the instance name. If the instance is part of a
     * cluster then the target name is the cluster name.
     *
     * @return the target name. 
     */
    public String getTargetName()
    {
        return mInstanceName;
    }

    /**
     * Get the Target Name for a specified instance. If the instance is not
     * clustered the instance name is returned. This operation is invoked by
     * the JBI instance MBeans only.
     *
     * @return the target name. 
     */
    public String getTargetName(String instanceName)
    {
        return instanceName;
    }
    
    /**
     * Get a set of the names of all the standalone servers in the domain.
     * @return a set of names of standalone servers in the domain.
     */
    public Set<String> getStandaloneServerNames()
    {
        HashSet<String> names = new HashSet<String>();
        names.add(mInstanceName);
        return names;
    }
    
    /**
     * Get a set of the names of all the clustered servers in the domain.
     * @return a set of names of clustered servers in the domain.
     */
    public Set<String> getClusteredServerNames()
    {
        return new HashSet<String>();
    }
    

    /**
     * Get a set of the names of all the clusters in the domain.
     * @return a set of names of clusters in the domain.
     */
    public Set<String> getClusterNames()
    {
        return new HashSet<String>();
    }
    
    /**
     * Get a set of the names of all the servers in the specified cluster.
     * @return a set of names of servers in the cluster.
     */
    public Set<String> getServersInCluster(String clusterName)
    {
        return new HashSet<String>();
    }
    
    /**
     * Determine whether a target is a valid server or cluster name.
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a valid
     * standalone server name or cluster name, <CODE>false</CODE> if not.
     */
    public boolean isValidTarget(String targetName)
    {
        return mInstanceName.equals(targetName);
    }
    
    /**
     * Determine whether a target is a cluster.
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a cluster,
     * <CODE>false</CODE> if not.
     */
    public boolean isCluster(String targetName)
    {
        return false;
    }
    
    /**
     * Determine whether a target is a standalone server.
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a standalone
     * server, <CODE>false</CODE> if not.
     */
    public boolean isStandaloneServer(String targetName)
    {
        return mInstanceName.equals(targetName);
    }
    
    /**
     * Determine whether the target is a clustered server.
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a clustered
     * server, <CODE>false</CODE> if not.
     */
    public boolean isClusteredServer(String targetName)
    {
        return false;
    }
    
    /**
     * Determine whether or not an instance is clustered.
     * @return <CODE>true</CODE> if the instance is clustered,
     * <CODE>false</CODE> if not.
     */
    public boolean isInstanceClustered(String instanceName)
    {
        return false;
    }

    /**
     * Get a string representation of the DAS JMX RMI connector port.
     * @return the JMX RMI connector port as a <CODE>String</CODE>.
     */
    public String getJmxRmiPort()
    {
        return mConnectorPort;
    }
    
    /**
     * Provides access to the platform's MBean server.
     * @return platform MBean server.
     */
    public MBeanServer getMBeanServer()
    {
        return java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }
    
    /**
     * Get the full path to the platform's instance root directory.
     * @return platform instance root
     */
    public String getInstanceRoot()
    {
        return mInstanceRoot;
    }
    
    /**
     * Get the full path to the platform's instaall root directory.
     * @return platform install root
     */
    public String getInstallRoot()
    {
        return mInstallRoot;
    }
    
    /**
     * Returns the provider type for this platform.
     * @return enum value corresponding to this platform implementation.
     */
    public JBIProvider getProvider()
    {
        return JBIProvider.JSE;
    }
    
    /**
     * Returns the KeyStoreUtil for Java SE.  Currently unsupported
     *
     * @return       a KeyStoreUtil
     * @exception    UnsupportedOperationException
     */
    public KeyStoreUtil getKeyStoreUtil() {
        return null;
    }

    /**
     *  Retrieves the naming context that should be used to locate platform
     *  resources (e.g. TransactionManager).
     *  @return naming context
     */
    public InitialContext getNamingContext()
    {
        return mNamingContext;
    }
    
    /**
     * Get the JBI system class loader for this implementation.
     * This is the JBI common classloader and is the parent of the JBI runtime
     * classloader that loaded this class.
     *
     * @return the <CODE>ClassLoader</CODE> that is the "system" class loader
     * from a JBI runtime perspective.
     * @throws SecurityException if access to the class loader is denied.
     */
    public ClassLoader getSystemClassLoader()
        throws SecurityException
    {
        return this.getClass().getClassLoader().getParent();
    }

    /**
     * Register a listener for platform events.
     * @param listener listener implementation
     */
    public void addListener(PlatformEventListener listener)
    {
        // NOP
    }
    
    
    /**
     * Remove a listener for platform events.
     * @param listener listener implementation
     */
    public void removeListener(PlatformEventListener listener)
    {
        // NOP
    }
    
    /**
     * Get the "com.sun.jbi" log level for a target.
     *
     * @param target - target name
     * @return the default platform log level
     */
    public java.util.logging.Level getJbiLogLevel(String target)
    {
        Level jbiLoggerLevel =
            Logger.getLogger(JBI_LOGGER_NAME).getLevel();
        
        if ( jbiLoggerLevel == null )
        {
            // -- If the level is not set return INFO
            jbiLoggerLevel = Level.INFO;
        }
        return jbiLoggerLevel;
    }
    
    
    /**
     * Set the "com.sun.jbi" log level for a target.
     *
     * @param target = target name
     * @param level the default platform log level
     */
    public void setJbiLogLevel(String target, java.util.logging.Level level)
    {
       Logger.getLogger(JBI_LOGGER_NAME).setLevel(level); 
    } 
}
