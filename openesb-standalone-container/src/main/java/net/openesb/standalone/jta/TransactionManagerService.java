package net.openesb.standalone.jta;

import com.atomikos.icatch.jta.UserTransactionManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.LifecycleException;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class TransactionManagerService implements Lifecycle {

    private static final Logger LOG = Logger.getLogger(TransactionManagerService.class.getName());

    @Inject
    private TransactionManager transactionManager;
    
    @Inject
    private InitialContext initialContext;
    
    @Override
    public void start() throws LifecycleException {
        UserTransactionManager utm = getTransactionManager();
                
        try {
            try {
                utm.init();
            } catch (SystemException ex) {
                Logger.getLogger(TransactionManagerProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            initialContext.createSubcontext("java:comp");
            initialContext.bind("java:comp/UserTransaction", utm);
        } catch (NamingException ex) {
            Logger.getLogger(TransactionManagerProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        UserTransactionManager utm = getTransactionManager();
        
        utm.close();
    }
    
    private UserTransactionManager getTransactionManager() {
        return (UserTransactionManager) transactionManager;
    }
}
