package net.openesb.standalone.jmx;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class MBServerConnectorFactory {

    private static final String DEFAULT_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi";
    private static final Logger LOG = Logger.getLogger(MBServerConnectorFactory.class.getName());
    private static MBeanServer server;
    private static boolean threaded;
    private static boolean daemon;
    private static int port;
    private static String serviceUrl;
    private static JMXConnectorServer connectorServer;

    private static class MBServerConnectorFactoryHolder {

        private static final MBServerConnectorFactory INSTANCE =
                new MBServerConnectorFactory();
    }

    private static class MBeanServerHolder {

        private static final MBeanServer INSTANCE =
                MBeanServerFactory.createMBeanServer();
    }

    private MBServerConnectorFactory() {
    }

    public static MBServerConnectorFactory getInstance() {
        return MBServerConnectorFactoryHolder.INSTANCE;
    }

    public void setPort(int newPort) {
        port = newPort;
    }
    
    public void setMBeanServer(MBeanServer ms) {
        server = ms;
    }

    public void setThreaded(boolean fthread) {
        threaded = fthread;
    }

    public void setDaemon(boolean fdaemon) {
        daemon = fdaemon;
    }
    
    private int getURLLocalHostPort(String url) {        
        int portStart = url.indexOf("localhost") + 10;
        int portEnd;
        int port = 0;
        if (portStart > 0) {
            portEnd = indexNotOfNumber(url, portStart);
            if (portEnd > portStart) {
                final String portString = url.substring(portStart, portEnd);
                port = Integer.parseInt(portString);               
            }
        }
        return port;
    }
    
    public String getServiceUrl() {
        return serviceUrl;
    }
    
    private static int indexNotOfNumber(String str, int index) {
        int i = 0;
        for (i = index; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
                return i;
            }
        }
        return -1;
    }

    public void createConnector() throws IOException {
        if (server == null) {
            server = MBeanServerHolder.INSTANCE;
        }

        serviceUrl = String.format(DEFAULT_SERVICE_URL, port);
        
        // Create the JMX service URL.
        JMXServiceURL url = new JMXServiceURL(serviceUrl);       
        
        // if the URL is localhost, start up an Registry
        if (serviceUrl.indexOf("localhost") > -1
            && url.getProtocol().compareToIgnoreCase("rmi") == 0) {
            try {
                int registryPort = getURLLocalHostPort(serviceUrl);
                try {
                    LocateRegistry.createRegistry(registryPort);
                } catch (Exception ex) {
                    // the registry may had been created
                    LocateRegistry.getRegistry(registryPort);
                }
               
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Create RMI registry failure: {0}", ex);
            }
        }
        
        HashMap<String, Object> environment = new HashMap<String, Object>();
     //   environment.put(JMXConnectorServer.AUTHENTICATOR, new JMXauthenticator(
     //               mPlatformContext.getSecurityProvider()));
      //      environment.put(Context.INITIAL_CONTEXT_FACTORY, RegistryContextFactory.class.getName());
        environment.put("com.sun.management.jmxremote.authenticate", Boolean.TRUE.toString());
            
        // Create the connector server now.
        connectorServer =
                JMXConnectorServerFactory.newJMXConnectorServer(url, environment, server);
        
        if (threaded) {
            // Start the connector server asynchronously (in a separate thread).
            Thread connectorThread = new Thread() {
                @Override
                public void run() {
                    try {
                        connectorServer.start();
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Start connector failure: {0}", ex);
                    }
                }
            };

            connectorThread.setName("JMX Connector Thread [" + connectorServer.getAddress() + "]");
            connectorThread.setDaemon(daemon);
            connectorThread.start();
        } else {
            // Start the connector server in the same thread.
            connectorServer.start();
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "JMX connector server started: {0}", connectorServer.getAddress());
        }
    }

    public void destroy() throws IOException {
        connectorServer.stop();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "JMX connector server stopped: {0}", connectorServer);
        }
    }
}
