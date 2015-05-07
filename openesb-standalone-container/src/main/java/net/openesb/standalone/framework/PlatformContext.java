package net.openesb.standalone.framework;

import com.sun.jbi.JBIProvider;
import com.sun.jbi.platform.PlatformEventListener;
import com.sun.jbi.security.KeyStoreUtil;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.inject.Inject;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.Constants;
import net.openesb.standalone.jmx.JMXService;
import net.openesb.standalone.node.Node;

/**
 * Implementation of PlatformContext for OpenESB Standalone.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PlatformContext implements com.sun.jbi.platform.PlatformContext {

    @Inject private JMXService jmxConnector;
    @Inject private SecurityProvider securityProvider;
    @Inject private TransactionManager transactionManager;
    @Inject private InitialContext namingContext;
    @Inject private Node node;
    
    private final KeyStoreUtil keyStoreUtil =
            new net.openesb.standalone.framework.KeyStoreUtil();
    
    private final String mInstallRoot;

    public PlatformContext() {
        mInstallRoot = System.getProperty(Constants.OPENESB_WORK_PROP,
                System.getProperty(Constants.OPENESB_HOME_PROP));
    }
    
    /**
     * Get the TransactionManager for this implementation. The instance returned
     * is an implementation of the standard JTS interface. If none is available,
     * returns
     * <CODE>null</CODE>.
     *
     * @return a <CODE>TransactionManager</CODE> instance.
     * @throws Exception if a <CODE>TransactionManager</CODE> cannot be
     * obtained.
     */
    @Override
    public javax.transaction.TransactionManager getTransactionManager()
            throws Exception {
        return transactionManager;
    }

    /**
     * Get the MBean server connection for a particular instance.
     *
     * @return the <CODE>MBeanServerConnection</CODE> for the specified
     * instance.
     */
    @Override
    public MBeanServerConnection getMBeanServerConnection(String instanceName)
            throws Exception {
        return java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Get the instance name of the platform's administration server. If the
     * platform does not provide a separate administration server, then this
     * method returns the name of the local instance.
     *
     * @return instance name of the administration server
     */
    @Override
    public String getAdminServerName() {
        return node.name();
    }

    /**
     * Determine whether this instance is the administration server instance.
     *
     * @return <CODE>true</CODE> if this instance is the administration server,
     * <CODE>false</CODE> if not.
     */
    @Override
    public boolean isAdminServer() {
        return true;
    }

    /**
     * Get the name of this instance.
     *
     * @return the name of this server instance.
     */
    @Override
    public String getInstanceName() {
        return node.name();
    }

    /**
     * Determine if the specified instance is up.
     *
     * @return true if the instance is up and running, false otherwise
     */
    @Override
    public boolean isInstanceUp(String instanceName) {
        return node.name().equals(instanceName);
    }

    /**
     * Determine whether multiple servers are permitted within this AS
     * installation.
     *
     * @return true if multiple servers are permitted.
     */
    @Override
    public boolean supportsMultipleServers() {
        return false;
    }

    /**
     * Get the Target Name. If the instance is not a clustered instance then the
     * target name is the instance name. If the instance is part of a cluster
     * then the target name is the cluster name.
     *
     * @return the target name.
     */
    @Override
    public String getTargetName() {
        return node.name();
    }

    /**
     * Get the Target Name for a specified instance. If the instance is not
     * clustered the instance name is returned. This operation is invoked by the
     * JBI instance MBeans only.
     *
     * @return the target name.
     */
    @Override
    public String getTargetName(String instanceName) {
        return instanceName;
    }

    /**
     * Get a set of the names of all the standalone servers in the domain.
     *
     * @return a set of names of standalone servers in the domain.
     */
    @Override
    public Set<String> getStandaloneServerNames() {
        HashSet<String> names = new HashSet<String>();
        names.add(node.name());
        return names;
    }

    /**
     * Get a set of the names of all the clustered servers in the domain.
     *
     * @return a set of names of clustered servers in the domain.
     */
    @Override
    public Set<String> getClusteredServerNames() {
        return new HashSet<String>();
    }

    /**
     * Get a set of the names of all the clusters in the domain.
     *
     * @return a set of names of clusters in the domain.
     */
    @Override
    public Set<String> getClusterNames() {
        return new HashSet<String>();
    }

    /**
     * Get a set of the names of all the servers in the specified cluster.
     *
     * @return a set of names of servers in the cluster.
     */
    @Override
    public Set<String> getServersInCluster(String clusterName) {
        return new HashSet<String>();
    }

    /**
     * Determine whether a target is a valid server or cluster name.
     *
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a valid
     * standalone server name or cluster name, <CODE>false</CODE> if not.
     */
    @Override
    public boolean isValidTarget(String targetName) {
        return node.name().equals(targetName);
    }

    /**
     * Determine whether a target is a cluster.
     *
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a cluster,
     * <CODE>false</CODE> if not.
     */
    @Override
    public boolean isCluster(String targetName) {
        return false;
    }

    /**
     * Determine whether a target is a standalone server.
     *
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a standalone
     * server, <CODE>false</CODE> if not.
     */
    @Override
    public boolean isStandaloneServer(String targetName) {
        return node.name().equals(targetName);
    }

    /**
     * Determine whether the target is a clustered server.
     *
     * @return <CODE>true</CODE> if <CODE>targetName</CODE> is a clustered
     * server, <CODE>false</CODE> if not.
     */
    @Override
    public boolean isClusteredServer(String targetName) {
        return false;
    }

    /**
     * Determine whether or not an instance is clustered.
     *
     * @return <CODE>true</CODE> if the instance is clustered,
     * <CODE>false</CODE> if not.
     */
    @Override
    public boolean isInstanceClustered(String instanceName) {
        return false;
    }

    /**
     * Get a string representation of the DAS JMX RMI connector port.
     *
     * @return the JMX RMI connector port as a <CODE>String</CODE>.
     */
    @Override
    public String getJmxRmiPort() {
        return Integer.toString(jmxConnector.getPort());
    }

    /**
     * Provides access to the platform's MBean server.
     *
     * @return platform MBean server.
     */
    @Override
    public MBeanServer getMBeanServer() {
        return jmxConnector.getMBeanServer();
    }

    /**
     * Get the full path to the platform's instance root directory.
     *
     * @return platform instance root
     */
    @Override
    public String getInstanceRoot() {
        return mInstallRoot + File.separator + node.name();
    }

    /**
     * Get the full path to the platform's instaall root directory.
     *
     * @return platform install root
     */
    @Override
    public String getInstallRoot() {
        return mInstallRoot;
    }

    /**
     * Returns the provider type for this platform.
     *
     * @return enum value corresponding to this platform implementation.
     */
    @Override
    public JBIProvider getProvider() {
        return JBIProvider.JSE;
    }

    /**
     * Returns the KeyStoreUtil for Java SE. Currently unsupported
     *
     * @return a KeyStoreUtil
     * @exception UnsupportedOperationException
     */
    @Override
    public KeyStoreUtil getKeyStoreUtil() {
        return keyStoreUtil;
    }

    /**
     * Retrieves the naming context that should be used to locate platform
     * resources (e.g. TransactionManager).
     *
     * @return naming context
     */
    @Override
    public InitialContext getNamingContext() {
        return namingContext;
    }

    /**
     * Get the JBI system class loader for this implementation. This is the JBI
     * common classloader and is the parent of the JBI runtime classloader that
     * loaded this class.
     *
     * @return the <CODE>ClassLoader</CODE> that is the "system" class loader
     * from a JBI runtime perspective.
     * @throws SecurityException if access to the class loader is denied.
     */
    @Override
    public ClassLoader getSystemClassLoader()
            throws SecurityException {
        return this.getClass().getClassLoader().getParent();
    }

    /**
     * Register a listener for platform events.
     *
     * @param listener listener implementation
     */
    @Override
    public void addListener(PlatformEventListener listener) {
        // NOP
    }

    /**
     * Remove a listener for platform events.
     *
     * @param listener listener implementation
     */
    @Override
    public void removeListener(PlatformEventListener listener) {
        // NOP
    }

    /**
     * Get the "com.sun.jbi" log level for a target.
     *
     * @param target - target name
     * @return the default platform log level
     */
    @Override
    public java.util.logging.Level getJbiLogLevel(String target) {
        Level jbiLoggerLevel =
                Logger.getLogger(JBI_LOGGER_NAME).getLevel();

        if (jbiLoggerLevel == null) {
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
    @Override
    public void setJbiLogLevel(String target, java.util.logging.Level level) {
        Logger.getLogger(JBI_LOGGER_NAME).setLevel(level);
    }
    
    @Override
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }
}
