package net.openesb.standalone.settings.yaml;

import com.google.inject.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import net.openesb.standalone.settings.ImmutableSettings;
import net.openesb.standalone.settings.Settings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class YamlSettingsProvider implements Provider<Settings> {

    private static final Logger mLog =
            Logger.getLogger(YamlSettingsProvider.class.getPackage().getName());
    
    private static final String CONFIG_FILE = "openesb.config";
    
    @Inject @Named("install.root")
    private String mInstallRoot;
    
    @Inject
    private Properties environment;
    
    @Override
    public Settings get() {
        String configFile = environment.getProperty(CONFIG_FILE);

        if (configFile == null) {
            configFile = mInstallRoot + File.separatorChar + "config/openesb.yaml";
        }

        mLog.log(Level.FINE, "Trying to load configuration from {0}", configFile);

        Settings settings;
        
        try {
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(),
                    new Resolver() {
                @Override
                protected void addImplicitResolvers() {
                }
            });
            InputStream input = new FileInputStream(new File(configFile));
            Map configurations = (Map) yaml.load(input);
            
            settings = new ImmutableSettings(configurations);
            mLog.log(Level.INFO, "Configuration loaded from {0}", configFile);
        } catch (FileNotFoundException fnfe) {
            mLog.log(Level.WARNING, "Unable to load configuration file {0}. Default configuration will be used.", configFile);
            settings = new ImmutableSettings(null);
        }
        
        return settings;
    }
}
