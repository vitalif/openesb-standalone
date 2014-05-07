package net.openesb.standalone.http.handlers;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class AdminConsoleHandler implements Handler<HttpHandler> {

    private final static String DUMMY_CLASS = "net.openesb.console.DummyClass";

    @Override
    public HttpHandler getHandler() {
        try {
            Class<?> clazz = Class.forName(
                    DUMMY_CLASS, false,
                    AdminConsoleHandler.class.getClassLoader());

            return new CLStaticHttpHandler(
                    clazz.getClassLoader(), "/public_html/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public String path() {
        return "/webui";
    }
}
