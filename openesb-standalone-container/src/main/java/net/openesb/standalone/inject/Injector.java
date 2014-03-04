package net.openesb.standalone.inject;

import com.google.inject.Guice;
import net.openesb.standalone.module.StandaloneModule;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class Injector {
    
    private static Injector instance;
    private com.google.inject.Injector injector;
    
    private Injector() {
        injector = Guice.createInjector(new StandaloneModule());
    }
    
    public static Injector getInstance() {
        if (instance == null) {
            instance = new Injector();
        }
        
        return instance;
    }
    
    public com.google.inject.Injector getInjector() {
        return this.injector;
    }
}
