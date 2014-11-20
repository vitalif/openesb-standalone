package net.openesb.standalone.naming.jndi.ds.tomcat;

import java.lang.reflect.Field;
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

        /*Return Fields declared in a class and its ancesters */
        Map<String, Field> dsFields = this.getAllFields(dsClass);

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

        /* Use java reflexion to set up Native Datasource declared in the context */
        Set<String> dspSet = datasourceMap.keySet();
        Iterator<String> keys = dspSet.iterator();
        while (keys.hasNext()) {
            String fieldName = keys.next();
            Field field = dsFields.get(fieldName);
            if (null == field) {
                LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.DS_DATASOURCE_PROPERTY_NOT_FOUND, fieldName, dspProperties.getDbConnectorName(), dsName));
            } else {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);

                String fieldValue = datasourceMap.get(fieldName);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.DS_DATASOURCE_PROPERTY_SET, fieldName, fieldValue, dspProperties.getDbConnectorName()));
                }

                try {
                    if (field.getType().equals(byte.class)) {
                        field.set(nativeDS, Byte.parseByte(fieldValue));
                    } else if (field.getType().equals(boolean.class)) {
                        field.set(nativeDS, Boolean.parseBoolean(fieldValue));
                    } else if (field.getType().equals(char.class)) {
                        field.set(nativeDS, fieldValue.charAt(0));
                    } else if (field.getType().equals(short.class)) {
                        field.set(nativeDS, Short.parseShort(fieldValue));
                    } else if (field.getType().equals(int.class)) {
                        field.set(nativeDS, Integer.parseInt(fieldValue));
                    } else if (field.getType().equals(long.class)) {
                        field.set(nativeDS, Long.parseLong(fieldValue));
                    } else if (field.getType().equals(float.class)) {
                        field.set(nativeDS, Float.parseFloat(fieldValue));
                    } else if (field.getType().equals(double.class)) {
                        field.set(nativeDS, Double.parseDouble(fieldValue));
                    } else if (field.getType().equals(String.class)) {
                        field.set(nativeDS, fieldValue);
                    } else {
                        LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                                LocalStringKeys.DS_DATASOURCE_PROPERTY_NOT_SET, fieldName, field.getType(), dspProperties.getDbConnectorName()));
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.DS_DATASOURCE_PROPERTY_INVALID_VALUE, fieldValue, fieldName));
                    throw ex;
                } catch (IllegalAccessException ex) {
                    LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.DS_DATASOURCE_PROPERTY_ACCESS, fieldName));
                    throw ex;
                } finally {
                    field.setAccessible(accessible);
                }
            }
        }

        /* Datasouce fields are set with data proterties found in the context 
         * Now let's set the pool with the pool proterties found in the context
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

        Class poolPropertiesClass = poolProperties.getClass();
        Map<String, Field> poolPropertiesFields = this.getAllFields(poolPropertiesClass);
        /* Use java reflexion to set up pool configurationwith context properties
         */
        Set<String> poolPropertiesSet = poolMap.keySet();
        keys = poolPropertiesSet.iterator();

        while (keys.hasNext()) {

            String fieldName = keys.next();
            Field field = poolPropertiesFields.get(fieldName);
            if (null == field) {
                LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                        LocalStringKeys.DS_POOL_PROPERTY_NOT_FOUND, fieldName, dspProperties.getDbConnectorName(), dsName));
            } else {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                
                String fieldValue = poolMap.get(fieldName);
                try {
                    if (field.getType().equals(byte.class)) {
                        field.set(poolProperties, Byte.parseByte(fieldValue));
                    } else if (field.getType().equals(boolean.class)) {
                        field.set(poolProperties, Boolean.parseBoolean(fieldValue));
                    } else if (field.getType().equals(char.class)) {
                        field.set(poolProperties, fieldValue.charAt(0));
                    } else if (field.getType().equals(short.class)) {
                        field.set(poolProperties, Short.parseShort(fieldValue));
                    } else if (field.getType().equals(int.class)) {
                        field.set(poolProperties, Integer.parseInt(fieldValue));
                    } else if (field.getType().equals(long.class)) {
                        field.set(poolProperties, Long.parseLong(fieldValue));
                    } else if (field.getType().equals(float.class)) {
                        field.set(poolProperties, Float.parseFloat(fieldValue));
                    } else if (field.getType().equals(double.class)) {
                        field.set(poolProperties, Double.parseDouble(fieldValue));
                    } else if (field.getType().equals(String.class)) {
                        field.set(poolProperties, fieldValue);
                    } else {
                        LOG.log(Level.WARNING, I18NBundle.getBundle().getMessage(
                                LocalStringKeys.DS_POOL_PROPERTY_NOT_SET, fieldName, field.getType(), dspProperties.getDbConnectorName()));
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.DS_POOL_PROPERTY_INVALID_VALUE, fieldValue, fieldName));
                    throw ex;
                } catch (IllegalAccessException ex) {
                    LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.DS_POOL_PROPERTY_ACCESS, fieldName));
                    throw ex;
                } finally {
                    field.setAccessible(accessible);
                }
            }
        }
        // set the pool and get a Poolled Datasource       
        poolProperties.setDataSource(nativeDS);
        poolProperties.setJmxEnabled(true);

        return poolProperties;
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

    /* getAll field is used to get all the fields declared in a class + its ancesters
     * getDeclaredField just returns the field declared at the class level and not 
     * at the ancester levels. The retured map contains key-values pairs with Field name and Field object
     */
    private Map<String, Field> getAllFields(Class<?> type) {
        List<Field> listFields = new ArrayList<Field>();
        for (Class<?> c = type; c
                != Object.class; c = c.getSuperclass()) {
            listFields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        Map<String, Field> mapFields = new HashMap<String, Field>();
        Iterator<Field> fieldIterator = listFields.iterator();

        while (fieldIterator.hasNext()) {
            Field field = fieldIterator.next();
            String fieldName = field.getName();
            mapFields.put(fieldName, field);
        }
        return mapFields;
    }
}
