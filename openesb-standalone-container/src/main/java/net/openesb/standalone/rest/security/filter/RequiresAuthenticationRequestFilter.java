package net.openesb.standalone.rest.security.filter;

import java.io.IOException;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
@Priority(Priorities.AUTHORIZATION)
public class RequiresAuthenticationRequestFilter implements ContainerRequestFilter {

    @Inject
    private SessionManager sessionManager;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("authorization");
        
        if (authHeader != null) {
            String token = decode(authHeader);

            Session session = sessionManager.getSession(token);
            if (session == null) {
                requestContext.abortWith(Response
                        .status(Response.Status.UNAUTHORIZED)
                        .build());
            }
        } else {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .build());
        }
    }

    private static String decode(String auth) {
        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
        return auth.replaceFirst("[T|t]oken ", "");
    }
}
