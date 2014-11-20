package net.openesb.standalone.naming;

import com.google.inject.Provider;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.naming.jndi.InitialContexFactoryImpl;
import net.openesb.standalone.settings.Settings;
import net.openesb.standalone.utils.I18NBundle;
import net.openesb.standalone.utils.StringUtils;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class ContextProvider implements Provider<InitialContext> {

    private static final Logger LOG =
            Logger.getLogger(ContextProvider.class.getPackage().getName());
    
    private static final String DEFAULT_CONTEXT_XML = "${openesb.home}/config/context.xml";
    private static final String CONTEXT_PATH = "jndi.context";
    
    @Inject
    private Settings settings;
    
    @Override
    public InitialContext get() {
        String context = getContext();
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_PATH, context));
        }
        
        InitialContext mNamingContext = null;
        
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, 
                    InitialContexFactoryImpl.class.getName());
            
            File contextFile = new File(context);
            if (! contextFile.exists()) {
                String msg = I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_INVALID_PATH, contextFile.getAbsolutePath());
               
                LOG.log(Level.SEVERE, msg);
                throw new IllegalStateException(msg);
            }
            
            env.put(Context.PROVIDER_URL, contextFile.toURI().toURL().toString());
            
            mNamingContext = new InitialContext(env);
        } catch (javax.naming.NamingException nmEx) {
            LOG.log(Level.SEVERE, "", nmEx);
        } catch (MalformedURLException nmEx) {
            LOG.log(Level.SEVERE, "", nmEx);
        }
        
        return mNamingContext;
    }
    
    private String getContext() {
        String context = settings.get(CONTEXT_PATH, DEFAULT_CONTEXT_XML);
        
        // Replace ${} with system properties
        return StringUtils.replace(context);
    }
}
