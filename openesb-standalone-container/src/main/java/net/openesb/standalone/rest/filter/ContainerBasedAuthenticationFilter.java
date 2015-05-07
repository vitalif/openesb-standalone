package net.openesb.standalone.rest.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import net.openesb.security.AuthenticationException;
import net.openesb.security.SecurityProvider;
import net.openesb.security.UsernamePasswordToken;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
@Provider
@PreMatching
public class ContainerBasedAuthenticationFilter implements ContainerRequestFilter {

    private final static Logger log = Logger.getLogger(ContainerBasedAuthenticationFilter.class.getName());

    @Inject
    private SecurityProvider securityProvider;

    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {

        // When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
        // In the real world, this should more sophisticated
        if (requestCtx.getRequest().getMethod().equals("OPTIONS")) {
            // Just send a OK signal back to the browser
            requestCtx.abortWith(Response.status(Response.Status.OK).build());
        } else {

            //try to authenticate
            String username = "";
            String password = "";

            String authorization = requestCtx.getHeaderString("authorization");

            if (null != authorization && authorization.length() > "Basic ".length()) {
                String usernamePassword = new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(authorization.substring("Basic ".length())));
                if (usernamePassword.contains(":")) {
                    username = usernamePassword.substring(0, usernamePassword.indexOf(":"));
                    if (usernamePassword.indexOf(":") + 1 < usernamePassword.length()) {
                        password = usernamePassword.substring(usernamePassword.indexOf(":") + 1);
                    }
                }
            }

            try {
                securityProvider.login(new UsernamePasswordToken(username, password));
            } catch (AuthenticationException aex) {
                log.log(Level.SEVERE, "Unexpected error while login: {0}", aex.getMessage());
                requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            String requestUriPath = requestCtx.getUriInfo().getRequestUri().getPath();

            if (requestUriPath.endsWith("/login")) {
                requestCtx.abortWith(Response.status(Response.Status.OK).build());
                return;
            }
        }
    }
}
