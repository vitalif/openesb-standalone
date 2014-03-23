package net.openesb.standalone.startup;

import java.lang.reflect.Method;
import java.util.logging.LogManager;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.node.Node;
import net.openesb.standalone.node.NodeBuilder;

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

        System.out.println("CONTAINER STARTED");
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

        // This is a fix when shutdown an instance by using a shutdown hook.
        try {
            invoke(LogManager.getLogManager(), "reset0");
        } catch (Throwable t) {
        }

        System.out.println("CONTAINER STOPPED");
    }

    /**
     * Utility method to invoke a method using reflection. This is kind of a
     * sloppy implementation, since we don't account for overloaded methods.
     *
     * @param obj contains the method to be invoked
     * @param method name of the method to be invoked
     * @param params parameters, if any
     * @return returned object, if any
     */
    private Object invoke(Object obj, String method, Object... params)
            throws Throwable {
        Object result = null;

        try {
            for (Method m : obj.getClass().getDeclaredMethods()) {
                if (m.getName().equals(method)) {
                    result = m.invoke(obj, params);
                    break;
                }
            }

            return result;
        } catch (java.lang.reflect.InvocationTargetException itEx) {
            throw itEx.getTargetException();
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
