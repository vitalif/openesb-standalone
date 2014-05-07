package net.openesb.standalone.env;

import com.google.inject.AbstractModule;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class EnvironmentModule extends AbstractModule {

    private final Environment environment;

    public EnvironmentModule(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(Environment.class).toInstance(environment);
    }
}
