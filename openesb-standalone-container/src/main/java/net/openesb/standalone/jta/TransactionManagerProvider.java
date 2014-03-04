package net.openesb.standalone.jta;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.google.inject.Provider;
import javax.transaction.TransactionManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {

    @Override
    public TransactionManager get() {
        return new UserTransactionManager();
    }
}
