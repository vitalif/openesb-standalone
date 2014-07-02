package net.openesb.standalone.http;

import net.openesb.standalone.Lifecycle;
import javax.ws.rs.core.Application;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface HttpServer extends Lifecycle {
    
    void addRestHandler(Application application, String rootURI);
}
