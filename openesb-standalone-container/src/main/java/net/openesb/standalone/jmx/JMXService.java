package net.openesb.standalone.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
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
import net.openesb.standalone.Constants;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.LifecycleException;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class JMXService implements Lifecycle {

    private static final Logger LOG = Logger.getLogger(JMXService.class.getName());
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

    @Override
    public void start() {
        if (server == null) {
            server = MBeanServerHolder.INSTANCE;
        }

        serviceUrl = String.format(Constants.DEFAULT_SERVICE_URL, getPort());
        JMXServiceURL url = null;

        try {
            // Create the JMX service URL.
            url = new JMXServiceURL(serviceUrl);
        } catch (MalformedURLException murle) {
            throw new LifecycleException("Unable to create JMX connector", murle);
        }

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

        try {
            // Create the connector server now.
            connectorServer =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, environment, server);
        } catch (IOException ioe) {
            throw new LifecycleException("Unable to create JMX connector", ioe);
        }

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
            try {
                // Start the connector server in the same thread.
                connectorServer.start();
            } catch (IOException ioe) {
                throw new LifecycleException("Unable to start JMX connector", ioe);
            }
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.CONNECTOR_START_CONNECTOR_STARTED,
                    connectorServer.getAddress()));
        }
    }

    @Override
    public void stop() {
        if (connectorServer != null) {
            try {
                connectorServer.stop();
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.CONNECTOR_SERVER_CONNECTOR_STOPPED));
                }
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.CONTAINER_SHUTDOWN_ERROR), ioe);
                throw new LifecycleException(
                        I18NBundle.getBundle().getMessage(
                            LocalStringKeys.CONTAINER_SHUTDOWN_ERROR), ioe);
            }
        }
    }

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

    public MBeanServer getMBeanServer() {
        return MBeanServerHolder.INSTANCE;
    }

    public int getPort() {
        try {
            return settings.getAsInt(CONNECTOR_PORT,
                    Constants.DEFAULT_INSTANCE_PORT);
        } catch (NumberFormatException nfEx) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.CONNECTOR_SERVER_INVALID_PORT,
                    Constants.DEFAULT_INSTANCE_PORT), nfEx);

            return Constants.DEFAULT_INSTANCE_PORT;
        }
    }
}
