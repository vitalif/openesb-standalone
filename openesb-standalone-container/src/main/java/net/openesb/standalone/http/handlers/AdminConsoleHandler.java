package net.openesb.standalone.http.handlers;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class AdminConsoleHandler implements Handler<HttpHandler> {

    @Override
    public HttpHandler getHandler() {
        return new RedirectHandler();
    }

    @Override
    public String path() {
        return "/webui";
    }
    
    static class RedirectHandler extends HttpHandler {

        @Override
        public void service(Request request, Response response) throws Exception {
            response.sendRedirect("/plugin/webui/");
        }
    }
}
