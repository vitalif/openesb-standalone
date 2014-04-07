package net.openesb.standalone.http;

import com.sun.jbi.EnvironmentContext;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import net.openesb.rest.api.OpenESBApplication;
import net.openesb.rest.extension.RestServiceLifecycle;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.LifecycleException;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.http.handlers.ConsoleHandler;
import net.openesb.standalone.jmx.JMXService;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.utils.I18NBundle;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.server.ContainerFactory;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class HttpService implements Lifecycle {

    private static final Logger LOG =
            Logger.getLogger(HttpService.class.getPackage().getName());
    private static final String HTTP_LISTENER_NAME = "openesb-http-server";
    private static final String HTTP_PORT_PROPERTY = "http.port";
    private static final String HTTP_ENABLED_PROPERTY = "http.enabled";
    private static final String HTTP_BINDING_PROPERTY = "http.binding";
    private static final int DEFAULT_HTTP_PORT = 4848;
    private static final boolean DEFAULT_HTTP_ENABLED = true;
    private HttpServer httpServer = null;
    @Inject
    private Settings settings;

    public void setEnvironmentContext(EnvironmentContext environmentContext) {
        RestServiceLifecycle.environmentContext = environmentContext;
    }

    @Override
    public void start() throws LifecycleException {
        boolean enabled = settings.getAsBoolean(HTTP_ENABLED_PROPERTY, DEFAULT_HTTP_ENABLED);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.HTTP_SERVER_ENABLED, enabled));
        }

        if (enabled) {
            httpServer = createHttpServer();

            // Map the path to the processor.
            final ServerConfiguration config = httpServer.getServerConfiguration();

            ConsoleHandler consoleHandler = new ConsoleHandler();
            config.addHttpHandler(consoleHandler.getHandler(), consoleHandler.path());

            HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, new OpenESBApplication());
            config.addHttpHandler(handler, "/api");

            try {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.HTTP_START_SERVER));
                }

                httpServer.start();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.HTTP_START_SERVER_FAILED), ex);
            }
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (httpServer != null) {
            httpServer.shutdownNow();
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.HTTP_STOP_SERVER));
            }
        }
    }

    private HttpServer createHttpServer() {
        int port = settings.getAsInt(HTTP_PORT_PROPERTY, DEFAULT_HTTP_PORT);
        String binding = settings.get(HTTP_BINDING_PROPERTY,
                NetworkListener.DEFAULT_NETWORK_HOST);

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.HTTP_SERVER_PORT, port));
        }

        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener(HTTP_LISTENER_NAME,
                binding, port);

        ThreadPoolConfig threadPoolConfig = ThreadPoolConfig
                .defaultConfig()
                .setCorePoolSize(5)
                .setMaxPoolSize(5);

        listener.getTransport().setWorkerThreadPoolConfig(threadPoolConfig);
        /*
         listener.setSecure(secure);
         if (sslEngineConfigurator != null) {
         listener.setSSLEngineConfig(sslEngineConfigurator);
         }
         */

        server.addListener(listener);
        return server;
    }
}
