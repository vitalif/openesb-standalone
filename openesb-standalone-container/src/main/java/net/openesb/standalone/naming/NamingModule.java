package net.openesb.standalone.naming;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import javax.naming.InitialContext;
import net.openesb.standalone.naming.jndi.DataSourcePoolFactory;
import net.openesb.standalone.naming.jndi.tomcat.TomcatDataSourcePoolFactory;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class NamingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InitialContext.class).toProvider(ContextProvider.class).in(Scopes.SINGLETON);
        bind(DataSourcePoolFactory.class).to(TomcatDataSourcePoolFactory.class).in(Scopes.SINGLETON);
    }
    
}
