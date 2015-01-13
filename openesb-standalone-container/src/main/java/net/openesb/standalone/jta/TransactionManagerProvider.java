package net.openesb.standalone.jta;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.TransactionManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {

    @Inject
    private InitialContext initialContext;
    
    @Override
    public TransactionManager get() {
        UserTransactionManager utm = new UserTransactionManager();
        
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
        
        return utm;
    }
}
