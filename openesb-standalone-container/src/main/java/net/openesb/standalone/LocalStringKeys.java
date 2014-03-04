package net.openesb.standalone;

/**
 * This interface contains the property keys used for looking up message text in
 * the resource bundle.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface LocalStringKeys {

    /**
     * Container Messages.
     */
    static final String CONTAINER_SHUTDOWN_ERROR =
            "CONTAINER_SHUTDOWN_ERROR";
    /**
     * Connector server Messages.
     */
    static final String CONNECTOR_CREATE_REGISTRY_FAILURE =
            "CONNECTOR_CREATE_REGISTRY_FAILURE";
    static final String CONNECTOR_START_CONNECTOR_FAILURE =
            "CONNECTOR_START_CONNECTOR_FAILURE";
    static final String CONNECTOR_START_CONNECTOR_STARTED =
            "CONNECTOR_START_CONNECTOR_STARTED";
    static final String CONNECTOR_SERVER_INVALID_PORT =
            "CONNECTOR_SERVER_INVALID_PORT";
    static final String CONNECTOR_SERVER_CONNECTOR_STOPPED =
            "CONNECTOR_SERVER_CONNECTOR_STOPPED";
    /**
     * Settings Messages.
     */
    static final String SETTINGS_LOAD_CONFIGURATION =
            "SETTINGS_LOAD_CONFIGURATION";
    static final String SETTINGS_CONFIGURATION_LOADED =
            "SETTINGS_CONFIGURATION_LOADED";
    static final String SETTINGS_CONFIGURATION_FAILURE =
            "SETTINGS_CONFIGURATION_FAILURE";
    
    /**
     * Security Messages.
     */
    static final String SECURITY_LOAD_CONFIGURATION =
            "SECURITY_LOAD_CONFIGURATION";
    static final String SECURITY_NO_REALM =
            "SECURITY_NO_REALM";
    static final String SECURITY_ADMIN_REALM_CONFIGURED =
            "SECURITY_ADMIN_REALM_CONFIGURED";
    static final String SECURITY_USER_REALM_CONFIGURED =
            "SECURITY_USER_REALM_CONFIGURED";
    static final String SECURITY_USER_REALM_ALREADY_DEFINED =
            "SECURITY_USER_REALM_ALREADY_DEFINED";
    static final String SECURITY_REALM_HANDLER_NOT_FOUND =
            "SECURITY_REALM_HANDLER_NOT_FOUND";
    static final String SECURITY_CREATE_PROPERTIES_REALM =
            "SECURITY_CREATE_PROPERTIES_REALM";
    static final String SECURITY_CREATE_PROPERTIES_REALM_INVALID_PATH =
            "SECURITY_CREATE_PROPERTIES_REALM_INVALID_PATH";
}
