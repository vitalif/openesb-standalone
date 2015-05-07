package net.openesb.standalone.rest;

import java.util.Set;
import net.openesb.rest.api.ManagementApplication;
import net.openesb.standalone.rest.filter.ContainerBasedAuthenticationFilter;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class ExtendedManagementApplication extends ManagementApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
       
        classes.add(ContainerBasedAuthenticationFilter.class);
        
        return classes;
    }
}
