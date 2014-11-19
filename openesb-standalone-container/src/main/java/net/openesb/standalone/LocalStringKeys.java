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
    static final String CONTAINER_INIT_INSTANCE =
            "CONTAINER_INIT_INSTANCE";
    static final String CONTAINER_INIT_INSTANCE_DONE =
            "CONTAINER_INIT_INSTANCE_DONE";
    static final String CONTAINER_START_INSTANCE =
            "CONTAINER_START_INSTANCE";
    static final String CONTAINER_START_INSTANCE_DONE =
            "CONTAINER_START_INSTANCE_DONE";
    static final String CONTAINER_STOP_INSTANCE =
            "CONTAINER_STOP_INSTANCE";
    static final String CONTAINER_STOP_INSTANCE_DONE =
            "CONTAINER_STOP_INSTANCE_DONE";
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
    /**
     * Naming Messages.
     */
    static final String NAMING_CONTEXT_PATH =
            "NAMING_CONTEXT_PATH";
    static final String NAMING_CONTEXT_INVALID_PATH =
            "NAMING_CONTEXT_INVALID_PATH";
    static final String NAMING_CONTEXT_NO_CONTEXT_URL =
            "NAMING_CONTEXT_NO_CONTEXT_URL";
    static final String NAMING_CONTEXT_CONTEXT_URL_INVALID =
            "NAMING_CONTEXT_CONTEXT_URL_INVALID";
    static final String NAMING_UNMARSHAL_FAILURE =
            "NAMING_UNMARSHAL_FAILURE";
    static final String NAMING_UNMARSHAL_SUCCESS =
            "NAMING_UNMARSHAL_SUCCESS";
    
    /**
     * HTTP Messages.
     */
    static final String HTTP_SERVER_PORT =
            "HTTP_SERVER_PORT";
    static final String HTTP_START_SERVER =
            "HTTP_START_SERVER";
    static final String HTTP_START_SERVER_FAILED =
            "HTTP_START_SERVER_FAILED";
    static final String HTTP_STOP_SERVER =
            "HTTP_STOP_SERVER";
    static final String HTTP_SERVER_ENABLED =
            "HTTP_SERVER_ENABLED";

    /**
     * Datasource Messages.
     */
    static final String DS_CLASS_NOT_FOUND =
            "DS_CLASS_NOT_FOUND";
    static final String DS_CREATE_DATASOURCE = 
            "DS_CREATE_DATASOURCE";
    static final String DS_UNABLE_TO_CREATE_DATASOURCE =
            "DS_UNABLE_TO_CREATE_DATASOURCE";
    static final String DS_UNABLE_TO_INSTANCIATE_CLASS =
            "DS_UNABLE_TO_INSTANCIATE_CLASS";
    static final String DS_UNABLE_TO_ACCESS_CLASS = 
            "DS_UNABLE_TO_ACCESS_CLASS";
    static final String DS_DATASOURCE_PROPERTIES_SETTLED = 
            "DS_DATASOURCE_PROPERTIES_SETTLED";
    static final String DS_DATASOURCE_PROPERTY_NOT_FOUND =
            "DS_DATASOURCE_PROPERTY_NOT_FOUND";
    static final String DS_UNABLE_TO_CREATE_MBEAN =
            "DS_UNABLE_TO_CREATE_MBEAN";
    static final String DS_DATASOURCE_PROPERTY_SET = 
            "DS_DATASOURCE_PROPERTY_SET";
    static final String DS_DATASOURCE_PROPERTY_NOT_SET =
            "DS_DATASOURCE_PROPERTY_NOT_SET";
    static final String DS_DATASOURCE_PROPERTY_INVALID_VALUE =
            "DS_DATASOURCE_PROPERTY_INVALID_VALUE";
    static final String DS_DATASOURCE_PROPERTY_ACCESS =
            "DS_DATASOURCE_PROPERTY_ACCESS";
    static final String DS_POOL_CONFIGURATION = 
            "DS_POOL_CONFIGURATION";
    static final String DS_POOL_PROPERTY_NOT_FOUND = 
            "DS_POOL_PROPERTY_NOT_FOUND";
    static final String DS_POOL_PROPERTY_SET = 
            "DS_POOL_PROPERTY_SET";
    static final String DS_POOL_PROPERTY_NOT_SET = 
            "DS_POOL_PROPERTY_NOT_SET";
    static final String DS_POOL_PROPERTY_INVALID_VALUE = 
            "DS_POOL_PROPERTY_INVALID_VALUE";
    static final String DS_POOL_PROPERTY_ACCESS = 
            "DS_POOL_PROPERTY_ACCESS"; 
}
