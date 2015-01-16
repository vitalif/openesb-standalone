package net.openesb.standalone.jta;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import javax.transaction.TransactionManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class TransactionModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(TransactionManager.class).toProvider(TransactionManagerProvider.class).in(Scopes.SINGLETON);
    }
}
