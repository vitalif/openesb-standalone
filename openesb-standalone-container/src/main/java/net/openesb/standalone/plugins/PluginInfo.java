package net.openesb.standalone.plugins;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class PluginInfo {

    public static final String DESCRIPTION_NOT_AVAILABLE = "No description found.";
    public static final String VERSION_NOT_AVAILABLE = "NA";
    private final String name;
    private final String description;
    private final boolean site;
    private final String version;

    /**
     * Information about plugins
     *
     * @param name Its name
     * @param description Its description
     * @param site true if it's a site plugin
     * @param version Version number is applicable (NA otherwise)
     */
    public PluginInfo(String name, String description, boolean site, String version) {
        this.name = name;
        this.description = description;
        this.site = site;
        if (version != null && !version.isEmpty()) {
            this.version = version;
        } else {
            this.version = VERSION_NOT_AVAILABLE;
        }
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean site() {
        return site;
    }

    public String version() {
        return version;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PluginInfo other = (PluginInfo) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PluginInfo{" + "name=" + name + ", description=" + description + ", site=" + site + ", version=" + version + '}';
    }
}
