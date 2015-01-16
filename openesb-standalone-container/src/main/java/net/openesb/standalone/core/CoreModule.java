package net.openesb.standalone.core;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.jmx.auth.login.JMXAuthenticator;
import net.openesb.standalone.security.SecurityProviderImpl;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SecurityProvider.class).to(SecurityProviderImpl.class).in(Scopes.SINGLETON);
        bind(javax.management.remote.JMXAuthenticator.class).to(JMXAuthenticator.class).in(Scopes.SINGLETON);
    }
}
