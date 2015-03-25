package net.openesb.standalone.plugins.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Arrays;
import net.openesb.model.api.Statistic;
import net.openesb.model.api.metric.Gauge;
import net.openesb.standalone.plugins.Plugin;
import net.openesb.standalone.plugins.PluginInfo;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginsModule extends Module {
    static final Version VERSION = new Version(1, 0, 0, "", "net.open-esb.standalone", "plugins-module");
    
    private static class PluginInfoSerializer extends StdSerializer<PluginInfo> {
        private PluginInfoSerializer() {
            super(PluginInfo.class);
        }

        @Override
        public void serialize(PluginInfo pluginInfo,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            try {
                json.writeObjectField("name", pluginInfo.name());
                json.writeObjectField("description", pluginInfo.description());
                json.writeObjectField("version", pluginInfo.version());
            //    json.writeObjectField("site", pluginInfo.site());
            } catch (RuntimeException e) {
                json.writeObjectField("error", e.toString());
            }
            json.writeEndObject();
        }
    }

    @Override
    public String getModuleName() {
        return "plugins-module";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addSerializers(new SimpleSerializers(Arrays.<JsonSerializer<?>>asList(
                new PluginInfoSerializer()
        )));
    }
}
