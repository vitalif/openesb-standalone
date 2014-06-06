package net.openesb.standalone.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.openesb.standalone.env.Environment;
import net.openesb.standalone.settings.Settings;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginsService {

    private static final Logger LOG = Logger.getLogger(PluginsService.class.getName());
    
    private final Environment environment;

    private final Set<Plugin> plugins;
    
    public PluginsService(Settings settings, Environment environment) {
        this.environment = environment;
        this.plugins = new HashSet<Plugin>();
        
        loadPlugins();
        
        loadSitePlugins();
    }

    private void loadPlugins() {
        
    }
    
    private Map<PluginInfo, Plugin> loadSitePlugins() {
        Map<PluginInfo, Plugin> sitePlugins = new HashMap<PluginInfo, Plugin>();

        // Let's try to find all _site plugins we did not already found
        File pluginsFile = environment.pluginsFile();

        if (!pluginsFile.exists() || !pluginsFile.isDirectory()) {
            return sitePlugins;
        }

        for (File pluginFile : pluginsFile.listFiles()) {
            File sitePluginDir = new File(pluginFile, "_site");
            if (sitePluginDir.exists()) {
                // There is a site plugin, let's try to get informations on it
                String name = pluginFile.getName();
                String version = "NA";
                String description = "No description found.";

                /*
                File pluginPropFile = new File(sitePluginDir, ES_PLUGIN_PROPERTIES);
                if (pluginPropFile.exists()) {

                    Properties pluginProps = new Properties();
                    InputStream is = null;
                    try {
                        is = new FileInputStream(pluginPropFile.getAbsolutePath());
                        pluginProps.load(is);
                        description = pluginProps.getProperty("description", PluginInfo.DESCRIPTION_NOT_AVAILABLE);
                        version = pluginProps.getProperty("version", PluginInfo.VERSION_NOT_AVAILABLE);
                    } catch (Exception e) {
                        // Can not load properties for this site plugin. Ignoring.
                        logger.debug("can not load {} file.", e, ES_PLUGIN_PROPERTIES);
                    } finally {
                        IOUtils.closeWhileHandlingException(is);
                    }
                }*/

                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "found a site plugin name [{}], version [{}], description [{}]",
                            new Object [] {name, version, description});
                }
                
                sitePlugins.put(
                        new PluginInfo(name, description, true, version), null);
            }
        }

        return sitePlugins;
    }
}
