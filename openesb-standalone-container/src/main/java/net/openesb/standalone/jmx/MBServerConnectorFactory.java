package net.openesb.standalone.jmx;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class MBServerConnectorFactory {

    private static final String DEFAULT_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi";
    private static final Logger LOG = Logger.getLogger(MBServerConnectorFactory.class.getName());
    private static final int DEFAULT_CONNECTOR_PORT = 8699;
    private static final String CONNECTOR_PORT = "instance.port";
    @Inject
    private Settings settings;
    @Inject
    private JMXAuthenticator authenticator;
    private MBeanServer server;
    private boolean threaded;
    private boolean daemon;
    private String serviceUrl;
    private JMXConnectorServer connectorServer;

    private static class MBeanServerHolder {

        private static final MBeanServer INSTANCE =
                java.lang.management.ManagementFactory.getPlatformMBeanServer();
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

        serviceUrl = String.format(DEFAULT_SERVICE_URL, getPort());

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
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.CONNECTOR_CREATE_REGISTRY_FAILURE), ex);
            }
        }

        HashMap<String, Object> environment = new HashMap<String, Object>();
        environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
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
                        LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                                LocalStringKeys.CONNECTOR_START_CONNECTOR_FAILURE, serviceUrl), ex);
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
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.CONNECTOR_START_CONNECTOR_STARTED,
                    connectorServer.getAddress()));
        }

    }

    public void destroy() throws IOException {
        if (connectorServer != null) {
            connectorServer.stop();
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.CONNECTOR_SERVER_CONNECTOR_STOPPED,
                        connectorServer));
            }
        }
    }

    public MBeanServer getMBeanServer() {
        return MBeanServerHolder.INSTANCE;
    }

    public int getPort() {
        try {
            return settings.getAsInt(CONNECTOR_PORT, DEFAULT_CONNECTOR_PORT);
        } catch (NumberFormatException nfEx) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.CONNECTOR_SERVER_INVALID_PORT, DEFAULT_CONNECTOR_PORT), nfEx);

            return DEFAULT_CONNECTOR_PORT;
        }
    }
}
