package net.openesb.standalone.http.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import net.openesb.standalone.env.Environment;
import org.glassfish.grizzly.http.io.OutputBuffer;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.http.util.MimeType;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class SitePluginHandler implements Handler<HttpHandler> {

    private final Environment environment;

    public SitePluginHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public HttpHandler getHandler() {
        return new LocalPluginHandler(path(), environment);
    }

    @Override
    public String path() {
        return "/plugin";
    }

    static class LocalPluginHandler extends HttpHandler {

        private final String root;
        private final Environment environment;

        public LocalPluginHandler(String root, Environment environment) {
            this.root = root;
            this.environment = environment;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            String path = request.getRequestURI().substring(root.length() + 1);
            int i1 = path.indexOf('/');

            String pluginName;
            String sitePath;
            
            if (i1 == -1) {
                response.sendRedirect(request.getRequestURI() + "/");
                return;
            } else {
                pluginName = path.substring(0, i1);
                sitePath = path.substring(i1 + 1);
            }

            if (sitePath.length() == 0) {
                sitePath = "/index.html";
            }

            // Convert file separators.
            sitePath = sitePath.replace('/', File.separatorChar);

            // this is a plugin provided site, serve it as static files from the plugin location
            File siteFile = new File(new File(environment.pluginsFile(), pluginName), "_site");
            File file = new File(siteFile, sitePath);
            if (!file.exists() || file.isHidden()) {
                response.setStatus(HttpStatus.NOT_FOUND_404);
                return;
            }
            if (!file.isFile()) {
                // If it's not a dir, we send a 403
                if (!file.isDirectory()) {
                    response.setStatus(HttpStatus.FORBIDDEN_403);
                    return;
                }
                // We don't serve dir but if index.html exists in dir we should serve it
                file = new File(file, "index.html");
                if (!file.exists() || file.isHidden() || !file.isFile()) {
                    response.setStatus(HttpStatus.FORBIDDEN_403);
                    return;
                }
            }
            if (!file.getAbsolutePath().startsWith(siteFile.getAbsolutePath())) {
                response.setStatus(HttpStatus.FORBIDDEN_403);
                return;
            }

            sendFile(response, file);
        }

        private void sendFile(final Response response, final File file)
                throws IOException {
            final String path = file.getPath();
            final FileInputStream fis = new FileInputStream(file);

            try {
                response.setStatus(HttpStatus.OK_200);
                String substr;
                int dot = path.lastIndexOf('.');
                if (dot < 0) {
                    substr = file.toString();
                    dot = substr.lastIndexOf('.');
                } else {
                    substr = path;
                }
                if (dot > 0) {
                    String ext = substr.substring(dot + 1);
                    String ct = MimeType.get(ext);
                    if (ct != null) {
                        response.setContentType(ct);
                    }
                } else {
                    response.setContentType(MimeType.get("html"));
                }

                final long length = file.length();
                response.setContentLengthLong(length);

                final OutputBuffer outputBuffer = response.getOutputBuffer();

                byte b[] = new byte[8192];
                int rd;
                while ((rd = fis.read(b)) > 0) {
                    outputBuffer.write(b, 0, rd);
                }
            } finally {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
