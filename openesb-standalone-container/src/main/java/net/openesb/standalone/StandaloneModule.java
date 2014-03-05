package net.openesb.standalone;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.sun.jbi.platform.PlatformContext;
import java.util.Properties;
import javax.transaction.TransactionManager;
import net.openesb.security.SecurityProvider;
import net.openesb.standalone.framework.StandaloneContainer;
import net.openesb.standalone.framework.StandalonePlatformContext;
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
public class StandaloneModule extends AbstractModule {

    private static final String DEFAULT_INSTALL_ROOT = System.getProperty("user.dir");
    private static final String INSTALL_ROOT = "install.root";
    
    @Override
    protected void configure() {
        bind(Properties.class).toInstance(StandaloneContainer.systemEnv);
        String installRoot = StandaloneContainer.systemEnv.getProperty(INSTALL_ROOT, DEFAULT_INSTALL_ROOT);
        bindConstant().annotatedWith(Names.named(INSTALL_ROOT)).to(installRoot);
        
        bind(PlatformContext.class).to(StandalonePlatformContext.class).asEagerSingleton();
        bind(Settings.class).toProvider(YamlSettingsProvider.class).asEagerSingleton();
        bind(TransactionManager.class).toProvider(TransactionManagerProvider.class).in(Scopes.SINGLETON);
        bind(SecurityProvider.class).to(SecurityProviderImpl.class).asEagerSingleton();
        bind(javax.management.remote.JMXAuthenticator.class).to(JMXAuthenticator.class).in(Scopes.SINGLETON);
    }
    
}
