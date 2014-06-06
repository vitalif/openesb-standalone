package net.openesb.standalone.plugins;

import com.google.inject.Module;
import java.util.Set;
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
    
    /**
     * 
     * @return 
     */
    String version();
    
    Set<Module> modules();
    
    Set<Lifecycle> services();
}
