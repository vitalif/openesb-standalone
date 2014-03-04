package net.openesb.standalone.naming;

import com.google.inject.Provider;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import net.openesb.standalone.naming.jndi.impl.InitialContexFactoryImpl;
import net.openesb.standalone.settings.yaml.YamlSettingsProvider;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class ContextProvider implements Provider<InitialContext> {

    private static final Logger LOG =
            Logger.getLogger(ContextProvider.class.getPackage().getName());
    
    @Inject @Named("install.root")
    private String installRoot;
    
    @Override
    public InitialContext get() {
        InitialContext mNamingContext = null;
        
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, 
                    InitialContexFactoryImpl.class.getName());
            
            File context = new File(installRoot + "/config/context.xml");
            env.put(Context.PROVIDER_URL, context.toURI().toURL().toString());
            
            mNamingContext = new InitialContext(env);
        } catch (javax.naming.NamingException nmEx) {
            LOG.log(Level.SEVERE, "", nmEx);
        } catch (MalformedURLException nmEx) {
            LOG.log(Level.SEVERE, "", nmEx);
        }
        
        return mNamingContext;
    }
    
}
