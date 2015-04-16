package net.openesb.standalone.naming.jndi;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.naming.jaxb.Context;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public abstract class AbstractContextFactory {

    private static final Logger LOG = Logger.getLogger(AbstractContextFactory.class.getName());

    protected Set<Context> loadContexts(String providerUrl) {
        if (providerUrl == null) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_NO_CONTEXT_URL));

            return Collections.EMPTY_SET;
        }

        try {
            File providerFile = new File(new URL(providerUrl).toURI());

            if (providerFile.isDirectory()) {
                // Load XML Files
                File[] potentialFiles = providerFile.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                });

                // Check if we can read them using JAXB
                return loadContexts(potentialFiles);
            } else {
                return loadContexts(providerFile);
            }

        } catch (Exception ex) {
            Logger.getLogger(AbstractContextFactory.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.EMPTY_SET;
        }
    }

    private Set<Context> loadContexts(File... contextFiles) {
        JAXBContextReader contextReader;

        try {
            contextReader = new JAXBContextReader();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return Collections.EMPTY_SET;
        }

        Set<Context> contexts = new HashSet<Context>(contextFiles.length);
        for (File contextFile : contextFiles) {
            try {
                LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.NAMING_CONTEXT_LOADING_URL, contextFile));

                Context context = contextReader.getContext(contextFile.toURI().toURL());
                contexts.add(context);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.NAMING_CONTEXT_CONTEXT_URL_INVALID, contextFile), ex);
            }
        }

        return contexts;
    }
}
