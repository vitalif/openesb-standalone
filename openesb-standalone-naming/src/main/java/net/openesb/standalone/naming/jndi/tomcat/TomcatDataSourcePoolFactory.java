package net.openesb.standalone.naming.jndi.tomcat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.bind.JAXBElement;
import net.openesb.standalone.naming.jndi.DataSourcePoolFactory;
import net.openesb.standalone.naming.utils.I18NBundle;
import net.openesb.standalone.oecontext.binding.DataSourcePoolPropertiesComplexType;
import net.openesb.standalone.oecontext.binding.DataSourcePropertiesComplexType;
import net.openesb.standalone.oecontext.binding.PoolPropertiesComplexType;
import net.openesb.standalone.oecontext.binding.PropertyComplexType;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public class TomcatDataSourcePoolFactory implements DataSourcePoolFactory {

    private final ResourceBundle mResourceBundle;
    private String mMessage;
    private final String mClassName = "DataSourcePoolFactoryimpl";
    private static final Logger sLogger = Logger.getLogger("net.openesb.standalone.naming");

    public TomcatDataSourcePoolFactory() {
        I18NBundle nBundle = new I18NBundle("net.openesb.standalone.naming.utils");
        mResourceBundle = nBundle.getBundle();
    }

    @Override
    /* GetDatasource method is used to create dynamically and set up a pooled datasource. Information and parameters
     * are provided by dspProperties. The first part of the method create dynamically a native datasource. 
     * Introspection is used to set up datasource properties. We setup just the properties declared in 
     * context.xml (or else).
     * Using the same way, the second part setup Apache pool. Important: Pool Datasource property is 
     * set up with the native datasource, so there is no need for setting up other pool properties
     * related to the connection. 
     * Then we create an Apache datasource with the pool as parameter
     */
    public DataSource getDataSource(DataSourcePoolPropertiesComplexType dspProperties) {
        PoolProperties poolProperties = this.createNativeDataSource(dspProperties);
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        ds.setName(dspProperties.getDbConnectorName());
        registerMBean(ds);

        return ds;
    }

    @Override
    public XADataSource getXADataSource(DataSourcePoolPropertiesComplexType dspProperties) {
        PoolProperties poolProperties = this.createNativeDataSource(dspProperties);
        org.apache.tomcat.jdbc.pool.XADataSource ds = new org.apache.tomcat.jdbc.pool.XADataSource(poolProperties);
        ds.setName(dspProperties.getDbConnectorName());
        registerMBean(ds);

        return ds;
    }

    private void registerMBean(org.apache.tomcat.jdbc.pool.DataSource ds) {
        try {
            ds.createPool();
            ds.setJmxEnabled(true);

            MBeanServer mBeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            
            String mBeanName = "net.open-esb.standalone:type=DataSources,name=" + ds.getName();
            mBeanServer.registerMBean(ds.getPool().getJmxPool(), new ObjectName(mBeanName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PoolProperties createNativeDataSource(DataSourcePoolPropertiesComplexType dspProperties) {
        String methodName = "createNativeDataSource";

        /* get the properties for the native Datasource. it is not created yet*/
        DataSourcePropertiesComplexType dataSourceProperties = dspProperties.getDataSourceProperties();
        Map<String, String> datasourceMap = this.listToMap(dataSourceProperties.getProperty());

        /* Get datasource name from OE Context. Native DS is create dynamically
         * so the class must be present in the classpath. DS Instance not created yet
         */
        String dsName = dspProperties.getDatasourceClassname();
        mMessage = mResourceBundle.getString("start.instanciate.datasource");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{dsName});
        Class<?> dsClass;
        try {
            dsClass = Class.forName(dsName);
        } catch (ClassNotFoundException ex) {
            mMessage = mResourceBundle.getString("datasource.class.not.found");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, new Object[]{dsName});
            mMessage = mResourceBundle.getString("catch.exception");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, ex);
            /* An exception don't stop JNDI context process so we return a null Datasource. */
            return null;
        }

        /*Return Fields declared in a class and its ancesters */
        Map<String, Field> dsFields = this.getAllFields(dsClass);
        /*Create datasource instance. This is the instance that will be set with reflexion and returned
         * to the caller
         */

        Object nativeDS;
        try {
            nativeDS = dsClass.newInstance();
        } catch (InstantiationException ex) {
            mMessage = mResourceBundle.getString("impossible.instanciate.datasource");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, new Object[]{dsName});
            mMessage = mResourceBundle.getString("catch.exception");
            sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
            return null;
        } catch (IllegalAccessException ex) {
            mMessage = mResourceBundle.getString("catch.exception");
            sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
            return null;
        }

        /* Use java reflexion to set up Native Datasource declared in the context */
        Set<String> dspSet = datasourceMap.keySet();
        Iterator<String> keys = dspSet.iterator();
        while (keys.hasNext()) {
            String fieldName = keys.next();
            Field field = dsFields.get(fieldName);
            if (null == field) {
                mMessage = mResourceBundle.getString("invalid.field.name");
                sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName, dsName});
            } else {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                String fieldValue = datasourceMap.get(fieldName);
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
                        mMessage = mResourceBundle.getString("field.not.set");
                        sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                        mMessage = mResourceBundle.getString("field.type.not.process");
                        sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName, dsName, field.getType()});
                    }
                } catch (IllegalArgumentException ex) {
                    mMessage = mResourceBundle.getString("field.not.set");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                    mMessage = mResourceBundle.getString("catch.exception");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
                } catch (IllegalAccessException ex) {
                    mMessage = mResourceBundle.getString("field.not.set");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                    mMessage = mResourceBundle.getString("catch.exception");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
                }
            }
        }

        /* Datasouce fields are set with data proterties found in the context 
         * Now let's set the pool with the pool proterties found in the context
         * get the properties for the pool */
        mMessage = mResourceBundle.getString("native.datasource.set.succesfully");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{dsName});

        /**
         * ** Set up Pool
         */
        mMessage = mResourceBundle.getString("start.pool.configuration");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage);
        PoolPropertiesComplexType contextPoolProperties = dspProperties.getPoolProperties();
        Map<String, String> poolMap = this.listToMap(contextPoolProperties.getProperty());
        // Create pool configuration
        org.apache.tomcat.jdbc.pool.PoolProperties poolProperties = new PoolProperties();
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
                mMessage = mResourceBundle.getString("invalid.field.name");
                sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName, poolPropertiesClass});
            } else {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
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
                        mMessage = mResourceBundle.getString("field.not.set");
                        sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                        mMessage = mResourceBundle.getString("field.type.not.process");
                        sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName, poolPropertiesClass, field.getType()});
                    }
                } catch (IllegalArgumentException ex) {
                    mMessage = mResourceBundle.getString("field.not.set");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                    mMessage = mResourceBundle.getString("catch.exception");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
                } catch (IllegalAccessException ex) {
                    mMessage = mResourceBundle.getString("field.not.set");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, new Object[]{fieldName});
                    mMessage = mResourceBundle.getString("catch.exception");
                    sLogger.logp(Level.INFO, mClassName, methodName, mMessage, ex);
                }
            }
        }
        // set the pool and get a Poolled Datasource       
        poolProperties.setDataSource(nativeDS);
        poolProperties.setJmxEnabled(true);

        return poolProperties;
    }

    /* List to Map is an internal methode used to convert a List<PropertyComplexType> to a Map.
     * Map will be use to set the DataSource and the Pool
     */
    private Map<String, String> listToMap(List<PropertyComplexType> inputList) {
        Map<String, String> outputMap = new HashMap<String, String>();
        Iterator<PropertyComplexType> it = inputList.iterator();
        while (it.hasNext()) {
            PropertyComplexType prop = it.next();
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
