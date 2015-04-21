package net.openesb.standalone.rest.resources;

import java.util.HashMap;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import net.openesb.rest.api.annotation.RequiresAuthentication;
import net.openesb.rest.api.resources.AbstractResource;
import net.openesb.security.AuthenticationException;
import net.openesb.security.SecurityProvider;
import net.openesb.security.UsernamePasswordToken;
import net.openesb.standalone.rest.security.Session;
import net.openesb.standalone.rest.security.SessionManager;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
@Path("authentication")
public class AuthenticationResource extends AbstractResource {

    @Inject
    private SecurityProvider securityProvider;
    @Inject
    private SessionManager sessionManager;
    
    @POST
    @Path("_login")
    public Response login(@Context ContainerRequestContext requestContext) {
        String auth = getAuthorizationHeader(requestContext);

        if (auth != null) {
            //lap : loginAndPassword
            String[] lap = decode(auth);

            //If login or password fail
            if (lap == null || lap.length != 2) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            try {
                securityProvider.login(
                        new UsernamePasswordToken(lap[0], lap[1]));

                Session session = sessionManager.create(new HashMap<String, Object>());
                return Response.ok().entity(session.getId()).build();
            } catch (AuthenticationException ae) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path("_logout")
    @RequiresAuthentication
    public Response logout(@Context Session session) {
        session.setExpired(true);
        
        return Response.ok().build();
    }

    private String getAuthorizationHeader(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString("authorization");

        if (auth == null) {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .build());

            return null;
        }

        return auth;
    }

    /**
     * Decode the basic auth and convert it to array login/password
     *
     * @param auth The string encoded authentification
     * @return The login (case 0), the password (case 1)
     */
    public static String[] decode(String auth) {
        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
        auth = auth.replaceFirst("[B|b]asic ", "");

        //Decode the Base64 into byte[]
        byte[] decodedBytes = null;

        try {
            decodedBytes = DatatypeConverter.parseBase64Binary(auth);
        } catch (Exception e) {
            // If we are not able to parse base64
        }

        //If the decode fails in any case
        if (decodedBytes == null || decodedBytes.length == 0) {
            return null;
        }

        //Now we can convert the byte[] into a splitted array :
        //  - the first one is login,
        //  - the second one password
        return new String(decodedBytes).split(":", 2);
    }
}
