package net.openesb.standalone.inject;

import com.google.inject.Guice;
import com.google.inject.Module;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class ModulesBuilder implements Iterable<Module> {

    private final List<Module> modules = new ArrayList<Module>();

    public ModulesBuilder add(Module... modules) {
        for (Module module : modules) {
            add(module);
        }
        return this;
    }

    public ModulesBuilder add(Module module) {
        modules.add(module);
        return this;
    }

    @Override
    public Iterator<Module> iterator() {
        return modules.iterator();
    }

    public com.google.inject.Injector createInjector() {
        com.google.inject.Injector injector = Guice.createInjector(modules);
        return injector;
    }
}
