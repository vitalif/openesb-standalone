package net.openesb.standalone.framework;

import com.sun.jbi.platform.PlatformContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.jbi.JBIException;

import net.openesb.standalone.Lifecycle;
import net.openesb.standalone.LifecycleException;
import net.openesb.standalone.settings.Settings;

/**
 * OpenESB Framework wrapper for OpenESB Standalone platform.
 * <br>
 * A FrameworkService instance cannot be loaded multiple times in the same VM.
 * If multiple instances of the framework are required in a VM, instantiate
 * multiple instances of JSEJBIFramework and load each one independently. There
 * is no limit on the number of uniquely named JSEJBIFramework instances in the
 * same VM. A specific JSEJBIFramework instance can be loaded and unloaded
 * multiple times in a VM.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class FrameworkService
        extends com.sun.jbi.framework.JBIFramework
        implements Lifecycle {

    private boolean mLoaded;
    private static final Logger LOG =
            Logger.getLogger(FrameworkService.class.getPackage().getName());
    @Inject
    private PlatformContext platformContext;
    @Inject
    private Settings settings;

    /**
     * Creates a new instance of the OpenESB framework.
     */
    public FrameworkService() {
        super();
    }

    @Override
    public void start() {
        if (mLoaded) {
            throw new LifecycleException("OpenESB Standalone runtime already loaded!");
        }
        
        // For stand-alone JBI, JBI_HOME = platform install root
        System.setProperty("com.sun.jbi.home",
                platformContext.getInstallRoot());

        // --------------------------------------------
        // TODO: removing this part asap
        System.setProperty("http.port",
                settings.get("http.port", "4848"));
        System.setProperty("http.enabled",
                settings.get("http.enabled", "true"));
        // --------------------------------------------

        try {
            init(platformContext, System.getProperties());
            startup(platformContext.getNamingContext(), "");
            prepare();
            ready(true);

            // JBI framework has been loaded
            mLoaded = true;
        } catch (JBIException jbie) {
            LOG.log(Level.SEVERE, "Unable to start properly OpenESB Core", jbie);
            throw new LifecycleException("Unable to start properly OpenESB Core", jbie);
        }
    }

    @Override
    public void stop() {
        if (!mLoaded) {
            return;
        }

        try {
            shutdown();
            terminate();

            mLoaded = false;
        } catch (JBIException jbie) {
            LOG.log(Level.SEVERE, "Unable to stop properly OpenESB Core", jbie);
            throw new LifecycleException("Unable to stop properly OpenESB Core", jbie);
        }
    }
}
