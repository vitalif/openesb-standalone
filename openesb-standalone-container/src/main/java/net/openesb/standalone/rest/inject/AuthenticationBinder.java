package net.openesb.standalone.rest.inject;


import javax.inject.Singleton;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionIdGenerator;
import net.openesb.standalone.rest.security.SessionManager;
import net.openesb.standalone.rest.security.impl.DefaultSessionManager;
import net.openesb.standalone.rest.security.impl.JavaUuidSessionIdGenerator;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class AuthenticationBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(DefaultSessionManager.class).to(SessionManager.class).in(Singleton.class);
        bind(JavaUuidSessionIdGenerator.class).to(SessionIdGenerator.class).in(Singleton.class);
        bindFactory(SessionInjectResolver.class).to(Session.class);
    }
    
}