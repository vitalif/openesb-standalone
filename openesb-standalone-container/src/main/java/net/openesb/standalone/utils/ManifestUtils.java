package net.openesb.standalone.utils;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class ManifestUtils {

    public static String getVersion() {
        Package aPackage = ManifestUtils.class.getPackage();
        return aPackage.getImplementationVersion();
    }
}
