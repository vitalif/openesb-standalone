package net.openesb.standalone.rest.feature;

import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import net.openesb.rest.api.annotation.RequiresAuthentication;
import net.openesb.standalone.rest.security.filter.RequiresAuthenticationRequestFilter;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class RequiresAuthenticationFeature implements DynamicFeature {

    private final static Logger LOG =
            Logger.getLogger(RequiresAuthenticationFeature.class.getName());
    
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        Method resourceMethod = resourceInfo.getResourceMethod();
        if (resourceClass.isAnnotationPresent(RequiresAuthentication.class)
                || resourceClass.getSuperclass().isAnnotationPresent(RequiresAuthentication.class)
                || resourceMethod.isAnnotationPresent(RequiresAuthentication.class)) {
            LOG.log(Level.FINE, "RESTAPI-xxxx: Add authentication feature for {0}", resourceInfo);
            context.register(RequiresAuthenticationRequestFilter.class);
        }
    }
}