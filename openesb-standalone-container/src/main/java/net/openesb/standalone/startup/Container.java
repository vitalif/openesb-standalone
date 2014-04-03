package net.openesb.standalone.startup;

import java.util.logging.LogManager;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.node.Node;
import net.openesb.standalone.node.NodeBuilder;
import net.openesb.standalone.utils.ReflectionUtils;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class Container implements Lifecycle {

    private final Node node;
    private Thread shutdownHook;

    public Container() {
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        node = nodeBuilder.build();
    }

    @Override
    public void start() {
        node.start();

        // Register shutdown hook
        shutdownHook = new ContainerShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Stop an existing server instance.
     */
    @Override
    public void stop() {
        node.stop();

        // This is a fix when we are trying to shutdown an instance by using 
        // a shutdown hook.
        try {
            ReflectionUtils.invoke(
                    LogManager.getLogManager(), "reset0");
        } catch (Throwable t) {
        }
    }

    private class ContainerShutdownHook extends Thread {

        @Override
        public void run() {
            if (node != null) {
                Container.this.stop();
            }
        }
    }
}
