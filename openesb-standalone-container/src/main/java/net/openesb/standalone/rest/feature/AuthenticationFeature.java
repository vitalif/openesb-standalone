package net.openesb.standalone.rest.feature;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import net.openesb.standalone.rest.inject.AuthenticationBinder;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class AuthenticationFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new AuthenticationBinder());
        
        return true;
    }
}