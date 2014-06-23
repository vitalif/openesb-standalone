package net.openesb.standalone.http;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import net.openesb.standalone.http.grizzly.EmbeddedHttpServer;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class HttpModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpServer.class).to(EmbeddedHttpServer.class).in(Scopes.SINGLETON);
    }
}
