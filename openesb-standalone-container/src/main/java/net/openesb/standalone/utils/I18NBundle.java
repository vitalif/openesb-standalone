package net.openesb.standalone.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class reads the i18n strings from locale specific bundle from the
 * Bundle[locale].properties or bundle[locale].properties file in a specified
 * package. This class has methods for formating the messages.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class I18NBundle {

    private static final Logger sLogger = 
            Logger.getLogger(I18NBundle.class.getName());
    
    /**
     * package name
     */
    private String mBundlePackageName = null;
    
    /**
     * resource bundle
     */
    private ResourceBundle mBundle = null;

    private static I18NBundle instance;
    
    /**
     * constructor
     *
     * @param packageName packe name ( e.g. com.sun.mypackage ) in which to look
     * for Bundle.properties file
     */
    private I18NBundle(String packageName) {
        this.mBundlePackageName = packageName;
        this.mBundle = null;
    }

    public static I18NBundle getBundle() {
        if (instance == null) {
            instance = new I18NBundle("net.openesb.standalone");
        }
        
        return instance;
    }
    
    /**
     * loads the bundle
     *
     * @param bundleName bundle name
     * @param packageName packe name ( e.g. com.sun.mypackage ) in which to look
     * for Bundle.properties file
     */
    private void loadBundle(String packageName, String bundleName) {

        String bundleBaseName = packageName + "." + bundleName;
        ResourceBundle resBundle = null;
        try {
            resBundle = ResourceBundle.getBundle(bundleBaseName);
        } catch (MissingResourceException ex) {
            // Try with locale independent defaultBundle
            try {
                resBundle = ResourceBundle.getBundle(bundleBaseName, new Locale(""));
            } catch (Exception anyEx) {
                sLogger.log(Level.FINE, anyEx.getMessage());
            }
        }

        if (resBundle != null) {
            this.mBundle = resBundle;
        }
    }

    /**
     * gets the loaded resource bundle
     *
     * @return resource bundle
     */
    private ResourceBundle getResourceBundle() {
        // lazzy init
        if (this.mBundle == null) {
            loadBundle(this.mBundlePackageName, "Bundle");
            // try to load the bundle with lower case first letter
            if (this.mBundle == null) {
                loadBundle(this.mBundlePackageName, "bundle");
            }
        }
        return this.mBundle;
    }

    /**
     * gets the i18n message
     *
     * @param aI18NMsg String.
     * @param aArgs Object[]
     * @return formated i18n string.
     */
    private static String getFormattedMessage(
            String aI18NMsg, Object[] aArgs) {
        return MessageFormat.format(aI18NMsg, aArgs);
    }

    /**
     * gets the i18n message
     *
     * @param aI18NKey i18n key
     * @param anArgsArray array of arguments for the formatted string
     * @return formatted i18n string
     */
    public String getMessage(String aI18NKey, Object[] anArgsArray) {
        String i18nMessage = getResourceBundle().getString(aI18NKey);
        if (anArgsArray != null) {
            return getFormattedMessage(i18nMessage, anArgsArray);
        } else {
            return i18nMessage;
        }
    }

    /**
     * gets the i18n message
     *
     * @param aI18NKey i18n key
     * @return i18n string
     */
    public String getMessage(String aI18NKey) {
        return getMessage(aI18NKey, null);
    }

    /**
     * gets the i18n message
     *
     * @param aI18NKey i18n key
     * @param arg1 argrument object to message
     * @return i18n string
     */
    public String getMessage(String aI18NKey, Object arg1) {
        Object[] args = {arg1};
        return getMessage(aI18NKey, args);
    }

    /**
     * gets the i18n message
     *
     * @param aI18NKey i18n key
     * @param arg1 argrument object to message
     * @param arg2 argrument object to message
     * @return i18n string
     */
    public String getMessage(String aI18NKey, Object arg1, Object arg2) {
        Object[] args = {arg1, arg2};
        return getMessage(aI18NKey, args);
    }

    /**
     * gets the i18n message
     *
     * @param aI18NKey i18n key
     * @param arg1 argrument object to message
     * @param arg2 argrument object to message
     * @param arg3 argrument object to message
     * @return i18n string
     */
    public String getMessage(String aI18NKey, Object arg1, Object arg2, Object arg3) {
        Object[] args = {arg1, arg2, arg3};
        return getMessage(aI18NKey, args);
    }
}