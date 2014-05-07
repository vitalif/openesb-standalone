package net.openesb.standalone.env;

import java.io.File;
import net.openesb.standalone.Constants;
import net.openesb.standalone.settings.Settings;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class Environment {
    
    private final Settings settings;
    
    private final File homeFile;
    
    private final File pluginsFile;
    
    public Environment(Settings settings) {
        this.settings = settings;
        
        homeFile = new File(
                System.getProperty(Constants.OPENESB_HOME_PROP));
        
        if (settings.get("path.plugins") != null) {
            pluginsFile = new File(settings.get("path.plugins"));
        } else {
            pluginsFile = new File(homeFile, "plugins");
        }
    }

    public Settings settings() {
        return this.settings;
    }

    public File hHomeFile() {
        return homeFile;
    }

    public File pluginsFile() {
        return pluginsFile;
    }
}
