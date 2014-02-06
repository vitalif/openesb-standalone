package net.openesb.standalone.settings;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface Settings {
    
    /**
     * Returns the setting value associated with the setting key.
     *
     * @param setting The setting key
     * @return The setting value, <tt>null</tt> if it does not exists.
     */
    String get(String setting);
    
    /**
     * Returns the setting value associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    String get(String setting, String defaultValue);
    
    /**
     * Returns the setting value (as int) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    Integer getAsInt(String setting, Integer defaultValue) throws SettingsException;
    
    /**
     * Returns the setting value (as boolean) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    Boolean getAsBoolean(String setting, Boolean defaultValue) throws SettingsException;
}
