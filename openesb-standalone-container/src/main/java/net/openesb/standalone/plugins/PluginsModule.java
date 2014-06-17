package net.openesb.standalone.plugins;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.openesb.standalone.settings.Settings;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginsModule extends AbstractModule {

    private final Settings settings;
    
    private final PluginsService pluginsService;
    
    public PluginsModule(Settings settings, PluginsService pluginsService) {
        this.settings = settings;
        this.pluginsService = pluginsService;
    }
    
    public Iterable<? extends Module> childModules() {
        List<Module> modules = new ArrayList<Module>();
        Collection<Class<? extends Module>> modulesClasses = pluginsService.modules();
        for (Class<? extends Module> moduleClass : modulesClasses) {
            modules.add(createModule(moduleClass, settings));
        }
        return modules;
    }
    
    @Override
    protected void configure() {
        bind(PluginsService.class).toInstance(pluginsService);
    }
    
    public static Module createModule(Class<? extends Module> moduleClass, Settings settings) {
        Constructor<? extends Module> constructor;
        try {
            constructor = moduleClass.getConstructor(Settings.class);
            try {
                return constructor.newInstance(settings);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create module [" + moduleClass + "]", e);
            }
        } catch (NoSuchMethodException e) {
            try {
                constructor = moduleClass.getConstructor();
                try {
                    return constructor.newInstance();
                } catch (Exception e1) {
                    throw new RuntimeException("Failed to create module [" + moduleClass + "]", e);
                }
            } catch (NoSuchMethodException e1) {
                throw new RuntimeException("No constructor for [" + moduleClass + "]");
            }
        }
    }
}
