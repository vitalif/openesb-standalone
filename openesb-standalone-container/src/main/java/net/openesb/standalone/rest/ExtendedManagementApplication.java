package net.openesb.standalone.rest;

import java.util.Set;
import net.openesb.rest.api.ManagementApplication;
import net.openesb.standalone.rest.feature.AuthenticationFeature;
import net.openesb.standalone.rest.feature.RequiresAuthenticationFeature;
import net.openesb.standalone.rest.resources.AuthenticationResource;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class ExtendedManagementApplication extends ManagementApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
       
        classes.add(AuthenticationResource.class);
        classes.add(AuthenticationFeature.class);
        classes.add(RequiresAuthenticationFeature.class);
                        
        return classes;
    }
}
