package net.openesb.standalone.naming.jndi.ds.tomcat;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.bind.JAXBElement;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.naming.jaxb.DataSourcePoolProperties;
import net.openesb.standalone.naming.jaxb.DataSourceProperties;
import net.openesb.standalone.naming.jaxb.PoolProperties;
import net.openesb.standalone.naming.jaxb.Property;
import net.openesb.standalone.naming.jndi.ds.DataSourcePoolFactory;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public class TomcatDataSourcePoolFactory implements DataSourcePoolFactory {

    private static final Logger LOG = Logger.getLogger(TomcatDataSourcePoolFactory.class.getName());

    /* GetDatasource method is used to create dynamically and set up a pooled datasource. Information and parameters
     * are provided by dspProperties. The first part of the method create dynamically a native datasource. 
     * Introspection is used to set up datasource properties. We setup just the properties declared in 
     * context.xml (or else).
     * Using the same way, the second part setup Apache pool. Important: Pool Datasource property is 
     * set up with the native datasource, so there is no need for setting up other pool properties
     * related to the connection. 
     * Then we create an Apache datasource with the pool as parameter
     */
    @Override
    public DataSource getDataSource(DataSourcePoolProperties dspProperties) {
        try {
            org.apache.tomcat.jdbc.pool.PoolProperties poolProperties = this.createNativeDataSource(dspProperties);
            org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
            ds.setName(dspProperties.getDbConnectorName());
            registerMBean(ds);

            return ds;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_UNABLE_TO_CREATE_DATASOURCE, dspProperties.getDbConnectorName()), ex);

            return null;
        }
    }

    @Override
    public XADataSource getXADataSource(DataSourcePoolProperties dspProperties) {
        try {
            org.apache.tomcat.jdbc.pool.PoolProperties poolProperties = this.createNativeDataSource(dspProperties);
            org.apache.tomcat.jdbc.pool.XADataSource ds = new org.apache.tomcat.jdbc.pool.XADataSource(poolProperties);
            ds.setName(dspProperties.getDbConnectorName());
            registerMBean(ds);

            return ds;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_UNABLE_TO_CREATE_DATASOURCE, dspProperties.getDbConnectorName()), ex);

            return null;
        }
    }

    private void registerMBean(org.apache.tomcat.jdbc.pool.DataSource ds) throws Exception {
        ds.createPool();

        try {
            ds.setJmxEnabled(true);

            MBeanServer mBeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer();

            String mBeanName = "net.open-esb.standalone:type=DataSources,name=" + ds.getName();
            mBeanServer.registerMBean(ds.getPool().getJmxPool(), new ObjectName(mBeanName));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_UNABLE_TO_CREATE_MBEAN, ds.getName()), ex);

        }
    }

    private org.apache.tomcat.jdbc.pool.PoolProperties createNativeDataSource(DataSourcePoolProperties dspProperties) throws Exception {
        /* get the properties for the native Datasource. it is not created yet*/
        DataSourceProperties dataSourceProperties = dspProperties.getDataSourceProperties();
        Map<String, String> datasourceMap = this.listToMap(dataSourceProperties.getProperty());

        /* Get datasource name from OE Context. Native DS is created dynamically
         * so the class must be present in the classpath. DS Instance not created yet
         */
        String dsName = dspProperties.getDatasourceClassname();

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_CREATE_DATASOURCE, dspProperties.getDbConnectorName()));
        }

        Class<?> dsClass;
        try {
            dsClass = Class.forName(dsName);
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_CLASS_NOT_FOUND, dsName, dspProperties.getDbConnectorName()));

            throw ex;
        }

        /*
         * Create datasource instance. 
         * 
         * This is the instance that will be set with reflexion and returned
         * to the caller
         */
        Object nativeDS;
        try {
            nativeDS = dsClass.newInstance();
        } catch (InstantiationException ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_UNABLE_TO_INSTANCIATE_CLASS, dsName, dspProperties.getDbConnectorName()));
            throw ex;
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_UNABLE_TO_ACCESS_CLASS, dsName, dspProperties.getDbConnectorName()));
            throw ex;
        }

        setAllProperties(nativeDS, datasourceMap, "DS_DATASOURCE_");

        /* Datasouce fields are set with data properties found in the context 
         * Now let's set the pool with the pool properties found in the context
         * get the properties for the pool */
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_DATASOURCE_PROPERTIES_SETTLED, dspProperties.getDbConnectorName()));
        }

        /**
         * ** Set up Pool
         */
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.DS_POOL_CONFIGURATION, dspProperties.getDbConnectorName()));
        }

        PoolProperties contextPoolProperties = dspProperties.getPoolProperties();
        Map<String, String> poolMap = this.listToMap(contextPoolProperties.getProperty());
        // Create pool configuration
        org.apache.tomcat.jdbc.pool.PoolProperties poolProperties
            = new org.apache.tomcat.jdbc.pool.PoolProperties();

        setAllProperties(poolProperties, poolMap, "DS_POOL_");

        // set the pool and get a Pooled Datasource
        poolProperties.setDataSource(nativeDS);
        poolProperties.setJmxEnabled(true);

        return poolProperties;
    }

    /**
     * Use java reflection to call setters on an object
     */
    private void setAllProperties(Object obj, Map<String, String> props, String errPrefix)
         throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class cl = obj.getClass();
        Map<String, Method> setters = getAllSetters(cl);
        Set<String> propSet = props.keySet();
        Iterator<String> keys = propSet.iterator();
        while (keys.hasNext()) {
            String fieldName = keys.next();
            Method m = setters.get(fieldName.toLowerCase());
            if (null == m) {
                LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                    errPrefix+"PROPERTY_NOT_FOUND", fieldName, cl.getName()));
                continue;
            }
            String fieldValue = props.get(fieldName);
            Class<?>[] paramTypes = m.getParameterTypes();
            if (paramTypes.length != 1) {
                LOG.log(Level.WARNING, "Method is not a setter or has more than 1 argument: "+cl.getName()+"."+m.getName());
                continue;
            }
            Class<?> t = paramTypes[0];
            try {
                if (t.equals(byte.class)) {
                    m.invoke(obj, Byte.parseByte(fieldValue));
                } else if (t.equals(boolean.class)) {
                    m.invoke(obj, Boolean.parseBoolean(fieldValue));
                } else if (t.equals(char.class)) {
                    m.invoke(obj, fieldValue.charAt(0));
                } else if (t.equals(short.class)) {
                    m.invoke(obj, Short.parseShort(fieldValue));
                } else if (t.equals(int.class)) {
                    m.invoke(obj, Integer.parseInt(fieldValue));
                } else if (t.equals(long.class)) {
                    m.invoke(obj, Long.parseLong(fieldValue));
                } else if (t.equals(float.class)) {
                    m.invoke(obj, Float.parseFloat(fieldValue));
                } else if (t.equals(double.class)) {
                    m.invoke(obj, Double.parseDouble(fieldValue));
                } else if (t.equals(String.class)) {
                    m.invoke(obj, fieldValue);
                } else {
                    LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                        errPrefix+"PROPERTY_NOT_SET", fieldName, t, cl.getName()));
                }
            } catch (InvocationTargetException ex) {
                throw ex;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    errPrefix+"PROPERTY_INVALID_VALUE", fieldValue, fieldName));
                throw ex;
            } catch (IllegalAccessException ex) {
                LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    errPrefix+"PROPERTY_ACCESS", fieldName));
                throw ex;
            }
        }
    }

    /* List to Map is an internal methode used to convert a List<Property> to a Map.
     * Map will be use to set the DataSource and the Pool
     */
    private Map<String, String> listToMap(List<Property> inputList) {
        Map<String, String> outputMap = new HashMap<String, String>();
        Iterator<Property> it = inputList.iterator();
        while (it.hasNext()) {
            Property prop = it.next();
            List<JAXBElement<String>> nameAndValueAndDescription = prop.getNameAndValueAndDescription();
            Iterator<JAXBElement<String>> it2 = nameAndValueAndDescription.iterator();
            String key = null, value = null;
            while (it2.hasNext()) {
                JAXBElement<String> element = it2.next();
                String localpart = element.getName().getLocalPart();
                if ("name".equals(localpart)) {
                    key = element.getValue();
                } else if ("value".equals(localpart)) {
                    value = element.getValue();
                }
                // Put the key valu in the Map
                outputMap.put(key, value);
            }
        }
        return outputMap;
    }

    /* getAllSetters is used to get all the setters declared in a class and its ancestors.
     * The retured map contains key-values pairs with lowercased field names and Method objects.
     */
    private Map<String, Method> getAllSetters(Class<?> type) {
        Map<String, Method> mapSetters = new HashMap<String, Method>();
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName().toLowerCase();
            if (name.startsWith("set"))
                mapSetters.put(name.substring(3), methods[i]);
        }
        return mapSetters;
    }
}
