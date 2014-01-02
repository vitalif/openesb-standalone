package net.openesb.standalone.naming.jndi.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.openesb.standalone.naming.jndi.DataSourcePoolFactory;
import net.openesb.standalone.naming.utils.I18NBundle;
import net.openesb.standalone.oecontext.binding.DataSourcePoolPropertiesComplexType;
import net.openesb.standalone.oecontext.binding.JdbcResourceComplexType;
import net.openesb.standalone.oecontext.binding.OeContextComplexType;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public class InitialContexFactoryImpl implements InitialContextFactory {

    public static final String RESOURCE_TYPE = "Datasource";
    private static final Logger sLogger = Logger.getLogger("net.openesb.standalone.naming");
    private final Map<String, DataSourcePoolPropertiesComplexType> mDSPMap = new HashMap<String, DataSourcePoolPropertiesComplexType>();
    private final DataSourcePoolFactory mDSPFactory = new DataSourcePoolFactoryimpl();
    private final String mClassName = "InitialContexFactoryImpl";    
    private final ResourceBundle mResourceBundle;
    private String mMessage;

    // Constructor
    public InitialContexFactoryImpl() {

        I18NBundle nBundle = new I18NBundle("net.openesb.standalone.naming.utils");
        mResourceBundle = nBundle.getBundle();
    }

    /* Regarding the exception management, If the context file if not correct, 
     * I choosed to return an initial context in any case even empty. So if input data 
     is not correct, I log this information but catch the exception in order to return 
     an initial context. Another policy would be to stop at any exception. I did not choose
     it. Naming exception will be thrown only if I cannot create the initial context  */
    /* The initial context I use is the one found in Tomcat 7 */
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Map<String, DataSource> datasourceMap = new HashMap<String, DataSource>();
        String methodName = "getInitialContext";
    //    /* Contect initialisation Just set the system properties and  use the class InitialContext*/
    //    System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    //    System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        Context initialContext = new InitialContext();
        mMessage = mResourceBundle.getString("context.created");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage);

        /* Second step read the XML file  URL where context configuration is described
         * The URL can be file:// http:// ... 
         * The XML File URL must be in environement hashmap and read the key URL must be equal to 
         * CONTEXT_URL*/
        String urlValue = null;
        if (environment.containsKey(Context.PROVIDER_URL)) {
            urlValue = (String) environment.get(Context.PROVIDER_URL);
            mMessage = mResourceBundle.getString("context.url.read");
            sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{urlValue});
        } else {
            mMessage = mResourceBundle.getString("context.url.not.provided") + " " + mResourceBundle.getString("context.url.not.provided.ID");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage);
        }

        /* Read the context from the URL */
        @SuppressWarnings("UnusedAssignment") JAXBElement<OeContextComplexType> root = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("net.openesb.standalone.oecontext.binding");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            root = (JAXBElement<OeContextComplexType>) unmarshaller.unmarshal(new URL(urlValue));
        } catch (MalformedURLException ex) {
            mMessage = mResourceBundle.getString("url.context.name.malformed") ;
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, new Object[] {urlValue}); 
            mMessage = mResourceBundle.getString("catch.exception");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, ex);
            return initialContext ;
        } catch (JAXBException ex) {
            mMessage = mResourceBundle.getString("jaxb.unmarshalling.failed");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, new Object[] {urlValue});
            mMessage = mResourceBundle.getString("catch.exception");
            sLogger.logp(Level.SEVERE, mClassName, methodName, mMessage, ex);
            return initialContext ;
        }

        // This must be made with the xml file has an element root
        // Log level Fine Unmarshalling ok       
        OeContextComplexType oeContext = root.getValue();
        mMessage = mResourceBundle.getString("context.binding.ok");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage);

        /* OeContext contains the complete context */
        /* I create a map with the datasourcePool Name as key and datasourcePool as Value
         * This will be useful to instanciate the db connector later.
         */
        List<DataSourcePoolPropertiesComplexType> dataSourcePoolList = oeContext.getDataSourcePoolProperties();
        int listSize = dataSourcePoolList.size();
        mMessage = mResourceBundle.getString("number.dataSourcePoolProperties.found");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{listSize});


        //Loop on dataSourcePoolList iterator
        for (DataSourcePoolPropertiesComplexType dspComplexType : dataSourcePoolList) {
            mDSPMap.put(dspComplexType.getDbConnectorName(), dspComplexType);
            mMessage = mResourceBundle.getString("datasourcepoolproperties.found.in.context");
            sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{dspComplexType.getDbConnectorName()});
        }

        // Now Let's read JdbcResource
        List<JdbcResourceComplexType> jdbcResourceList = oeContext.getJdbcResources();
        listSize = jdbcResourceList.size();
        mMessage = mResourceBundle.getString("number.jdbcResource.declaration.found");
        sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{listSize});

        //Loop on JDBCResourceList iterator
        for (JdbcResourceComplexType jdbcResource : jdbcResourceList) {
            /* For each jcbc resource I want to associate a dbConnector. 
             * DBConnector provide a connectionPool or a XAConnectionPool
             * I instanciate the dbConnetor in a lazy mode (when needed)
             * Once instanciated dbConnector are put in a map for reusing purpose
             * ex: when two JNDI names target the same dbConnector
             */

            // Get JNDI Name 
            String jndiName = jdbcResource.getJndiName();
            mMessage = jndiName + " " + mResourceBundle.getString("in.process");
            sLogger.logp(Level.FINE, mClassName, methodName, mMessage);


            /* Check if this JNDI name is already in the context. In that case the 
             * second instance is not taken into account 
             */
            try {
                initialContext.lookup(jndiName);
                mMessage = mResourceBundle.getString("jndi.value.already.defined");
                sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{jndiName});
                continue;
            } catch (NamingException ex) {
                // Nothing else to do. Having an exception is the normal process
            }


            /* Create datasource or XA Datasource thanks to the underlying dbConnector 
             * DBConnector refeence is in the DataSourcePoolProperties. DBConnector are instanciated
             * dynamically and must be present in the classpath
             * */
            String dbConnectorName = jdbcResource.getDbConnectorName();
            /* check if the datasource has been created already for a previous 
             * JNDI Name. In that case we reuse it. 
             */
            if (datasourceMap.containsKey(dbConnectorName)) {
                if (datasourceMap.get(dbConnectorName) instanceof XADataSource) {
                    initialContext.rebind(jndiName, (XADataSource) datasourceMap.get(dbConnectorName));
                } else {
                    initialContext.rebind(jndiName, datasourceMap.get(dbConnectorName));
                }
                continue;
            }

            // Retrieve DataSourcePoolPropertie
            DataSourcePoolPropertiesComplexType dspProperties = mDSPMap.get(dbConnectorName);
            // Check if Datasourse or XA Datasource            
            if (dspProperties.getResourceType().equals(InitialContexFactoryImpl.RESOURCE_TYPE)) {
                mMessage = mResourceBundle.getString("datasource.in.process");
                sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{jndiName});
                DataSource dataSource = mDSPFactory.getDataSource(dspProperties);
                /* Check if datasource is not null then put in the context since exception are catch */
                if (null != dataSource) {
                    datasourceMap.put(dbConnectorName, dataSource);
                    initialContext.rebind(jndiName, dataSource);
                    mMessage = mResourceBundle.getString("datasource.processed.bind.success");
                    sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{jndiName});
                }

            } else if (dspProperties.getResourceType()
                    .equals(InitialContexFactoryImpl.RESOURCE_TYPE)) {
                mMessage = mResourceBundle.getString("xadatasource.in.process");
                sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{jndiName});
                XADataSource xaDataSource = mDSPFactory.getXADataSource(dspProperties);
                if (null != xaDataSource) {
                    /* Store the XAdatasource in a map for reusing purpose see above */
                    datasourceMap.put(dbConnectorName, (DataSource) xaDataSource);
                    initialContext.rebind(jndiName, xaDataSource);
                    mMessage = mResourceBundle.getString("xadatasource.processed.bind.success");
                    sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{jndiName});
                }
            } else {
                mMessage = mResourceBundle.getString("bad.resource.type");
                sLogger.logp(Level.FINE, mClassName, methodName, mMessage, new Object[]{dspProperties.getResourceType(), dspProperties.getDatabaseName()});

            }
        }

        /* the context contains the binding and the datasource or xaDatasource links 
         * Return the context
         */
        return initialContext;
    }
}
