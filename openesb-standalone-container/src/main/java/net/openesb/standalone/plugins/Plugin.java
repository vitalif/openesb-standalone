package net.openesb.standalone.plugins;

import com.google.inject.Module;
import java.util.Collection;
import net.openesb.standalone.Lifecycle;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface Plugin {
    
    /**
     * The name of the plugin.
     */
    String name();

    /**
     * The description of the plugin.
     */
    String description();
    
    Collection<Class<? extends Module>> modules();
    
    Collection<Class<? extends Lifecycle>> services();
}
