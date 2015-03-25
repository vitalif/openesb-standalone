package net.openesb.standalone.plugins;

import com.google.inject.Module;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.env.Environment;
import net.openesb.standalone.settings.Settings;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginsService {

    private static final Logger LOGGER = Logger.getLogger(PluginsService.class.getName());
    
    private static final String PLUGIN_PROPERTIES = "plugin.properties";
    
    private final Settings settings;
    private final Environment environment;
    private final Set<Plugin> plugins;
    private Set<PluginInfo> pluginInfos;

    public PluginsService(Settings settings, Environment environment) {
        this.settings = settings;
        this.environment = environment;

        this.pluginInfos = new HashSet<PluginInfo>();
        // Load plugins in the classloader
        loadPluginsIntoClassLoader();
        this.plugins = loadPluginsFromClasspath();
    }

    private void loadPluginsIntoClassLoader() {
        File pluginsFile = environment.pluginsFile();
        if (!pluginsFile.exists() || !pluginsFile.isDirectory()) {
            return;
        }

        ClassLoader classLoader = getCurrentClassloader();
        Class classLoaderClass = classLoader.getClass();
        Method addURL = null;
        while (!classLoaderClass.equals(Object.class)) {
            try {
                addURL = classLoaderClass.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                break;
            } catch (NoSuchMethodException e) {
                // no method, try the parent
                classLoaderClass = classLoaderClass.getSuperclass();
            }
        }
        if (addURL == null) {
            LOGGER.log(Level.INFO, "failed to find addURL method on classLoader [{0}] to add methods", classLoader);
            return;
        }
        
        for (File pluginFile : pluginsFile.listFiles()) {
            if (pluginFile.isDirectory()) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "--- Adding plugin [{0}]", pluginFile.getAbsolutePath());
                }

                try {
                    // add the root
                    addURL.invoke(classLoader, pluginFile.toURI().toURL());
                    
                    List<File> libFiles = new ArrayList<File>();
                    if (pluginFile.listFiles() != null) {
                        libFiles.addAll(Arrays.asList(pluginFile.listFiles()));
                    }
                    File libLocation = new File(pluginFile, "lib");
                    if (libLocation.exists() && libLocation.isDirectory() && libLocation.listFiles() != null) {
                        libFiles.addAll(Arrays.asList(libLocation.listFiles()));
                    }

                    for (File libFile : libFiles) {
                        if (!(libFile.getName().endsWith(".jar") || libFile.getName().endsWith(".zip"))) {
                            continue;
                        }
                        addURL.invoke(classLoader, libFile.toURI().toURL());
                    }
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "Failed to add plugin [" + pluginFile + "]", e);
                }
            }
        }
    }
    
    private Set<Plugin> loadPluginsFromClasspath() {
        Set<Plugin> plugins = new HashSet<Plugin>();
        
        try {
            ClassLoader classLoader = getCurrentClassloader();
            Enumeration<URL> pluginUrls = classLoader.getResources(PLUGIN_PROPERTIES);
            while (pluginUrls.hasMoreElements()) {
                URL pluginUrl = pluginUrls.nextElement();
                Properties pluginProps = new Properties();
                InputStream is = null;
                try {
                    is = pluginUrl.openStream();
                    pluginProps.load(is);
                    String pluginClassName = pluginProps.getProperty("plugin");
                    String pluginVersion = pluginProps.getProperty("version", PluginInfo.VERSION_NOT_AVAILABLE);
                    Plugin plugin = loadPlugin(pluginClassName, settings);

                    PluginInfo info = new PluginInfo(plugin.name(), plugin.description(), true, pluginVersion);
                    
                    plugins.add(plugin);
                    pluginInfos.add(info);
                    
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, "Failed to load plugin from [" + pluginUrl + "]", e);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to find plugins from classpath", e);
        }
        
        return plugins;
    }
    
    private Plugin loadPlugin(String className, Settings settings) {
        try {
            Class<? extends Plugin> pluginClass = (Class<? extends Plugin>) getCurrentClassloader().loadClass(className);
            Plugin plugin;
            try {
                plugin = pluginClass.getConstructor(Settings.class).newInstance(settings);
            } catch (NoSuchMethodException e) {
                try {
                    plugin = pluginClass.getConstructor().newInstance();
                } catch (NoSuchMethodException nsme) {
                    throw new RuntimeException("No constructor for [" + pluginClass + "]. A plugin class must " +
                            "have either an empty default constructor or a single argument constructor accepting a " +
                            "Settings instance");
                }
            }

            return plugin;

        } catch (Throwable e) {
            throw new RuntimeException("Failed to load plugin class [" + className + "]", e);
        }
    }
    
    private ClassLoader getCurrentClassloader() {
        return PluginsService.class.getClassLoader();
    }
    
    public Set<Plugin> plugins() {
        return plugins;
    }
    
    public Set<PluginInfo> pluginInfos() {
        return pluginInfos;
    }
    
    public Collection<Class<? extends Lifecycle>> services() {
        List<Class<? extends Lifecycle>> services = new ArrayList<Class<? extends Lifecycle>>();
        for (Plugin plugin : plugins) {
            services.addAll(plugin.services());
        }
        return services;
    }
    
    public Collection<Class<? extends Module>> modules() {
        List<Class<? extends Module>> modules = new ArrayList<Class<? extends Module>>();
        for (Plugin plugin : plugins) {
            modules.addAll(plugin.modules());
        }
        return modules;
    }
}
