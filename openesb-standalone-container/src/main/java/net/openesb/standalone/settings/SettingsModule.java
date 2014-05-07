package net.openesb.standalone.settings;

import com.google.inject.AbstractModule;


/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SettingsModule extends AbstractModule {

    private final Settings settings;

    public SettingsModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(Settings.class).toInstance(settings);
    }
}