package net.openesb.standalone.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An immutable implementation of {@link Settings}.
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class ImmutableSettings implements Settings {

    private final Map<String, String> settings;
    
    public ImmutableSettings(Map<String, String> settings) {
        if (settings != null) {
            this.settings = Collections.unmodifiableMap(settings);
        } else {
            this.settings = Collections.unmodifiableMap(new HashMap<String, String>());
        }
    }
    
    @Override
    public String get(String setting) {
        String retVal = settings.get(setting);
        if (retVal != null) {
            return retVal;
        }
        
        return null;
    }

    @Override
    public String get(String setting, String defaultValue) {
        String retVal = get(setting);
        return retVal == null ? defaultValue : retVal;
    }
    
    @Override
    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
        return parseBoolean(get(setting), defaultValue);
    }
    
    @Override
    public Integer getAsInt(String setting, Integer defaultValue) throws SettingsException {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse int setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }
    
    public static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    @Override
    public Object getAsObject(String setting) throws SettingsException {
        return settings.get(setting);
    }
}