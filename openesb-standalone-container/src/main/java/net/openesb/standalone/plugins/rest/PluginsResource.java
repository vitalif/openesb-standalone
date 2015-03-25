package net.openesb.standalone.plugins.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.openesb.standalone.plugins.PluginsService;
import net.openesb.standalone.plugins.jackson.PluginsModule;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
@Path("/")
public class PluginsResource {
    
    private static final ObjectMapper mapper = new ObjectMapper().registerModules(
            new PluginsModule());
    
    @Inject
    private PluginsService pluginsService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String pluginInfos() throws JsonProcessingException {
        return mapper.writeValueAsString(pluginsService.pluginInfos());
    }
}
