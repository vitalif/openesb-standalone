package net.openesb.standalone.framework;

import com.google.inject.AbstractModule;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class FrameworkModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(com.sun.jbi.platform.PlatformContext.class)
                .to(net.openesb.standalone.framework.PlatformContext.class)
                .asEagerSingleton();
    }
}
