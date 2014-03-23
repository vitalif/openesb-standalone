package net.openesb.standalone.core;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import javax.transaction.TransactionManager;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.jmx.auth.login.JMXAuthenticator;
import net.openesb.standalone.jta.TransactionManagerProvider;
import net.openesb.standalone.security.SecurityProviderImpl;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.settings.yaml.YamlSettingsProvider;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Settings.class).toProvider(YamlSettingsProvider.class).asEagerSingleton();
        bind(TransactionManager.class).toProvider(TransactionManagerProvider.class).in(Scopes.SINGLETON);
        bind(SecurityProvider.class).to(SecurityProviderImpl.class).asEagerSingleton();
        bind(javax.management.remote.JMXAuthenticator.class).to(JMXAuthenticator.class).in(Scopes.SINGLETON);
    }
}
