package net.openesb.standalone.naming.jndi.impl;

import net.openesb.standalone.naming.jndi.tomcat.TomcatDataSourcePoolFactory;
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
import net.openesb.standalone.naming.jaxb.DataSourcePoolPropertiesComplexType;
import net.openesb.standalone.naming.jaxb.JdbcResourceComplexType;
import net.openesb.standalone.naming.jaxb.OeContextComplexType;
import net.openesb.standalone.naming.jndi.DataSourcePoolFactory;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public class InitialContexFactoryImpl implements InitialContextFactory {

    private static final Logger LOG = Logger.getLogger(InitialContexFactoryImpl.class.getName());
    public static final String DATASOURCE_TYPE = "Datasource";
    public static final String XADATASOURCE_TYPE = "XADatasource";
    private final Map<String, DataSourcePoolPropertiesComplexType> mDSPMap = new HashMap<String, DataSourcePoolPropertiesComplexType>();
    private final DataSourcePoolFactory mDSPFactory = new TomcatDataSourcePoolFactory();
    private final String mClassName = "InitialContexFactoryImpl";


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
        /*Context initialisation Just set the system properties and  use the class InitialContext*/
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        Context initialContext = new InitialContext();

        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("context.created"));

        /* Second step read the XML file  URL where context configuration is described
         * The URL can be file:// http:// ... 
         * The XML File URL must be in environement hashmap and read the key URL must be equal to 
         * CONTEXT_URL*/
        String urlValue = null;
        if (environment.containsKey(Context.PROVIDER_URL)) {
            urlValue = (String) environment.get(Context.PROVIDER_URL);
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("context.url.read", urlValue));
        } else {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage("context.url.not.provided"));
        }

        /* Read the context from the URL */
        @SuppressWarnings("UnusedAssignment")
        JAXBElement<OeContextComplexType> root = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("net.openesb.standalone.naming.jaxb");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            root = (JAXBElement<OeContextComplexType>) unmarshaller.unmarshal(new URL(urlValue));
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage("url.context.name.malformed", urlValue));
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage("catch.exception"), ex);
            return initialContext;
        } catch (JAXBException ex) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage("jaxb.unmarshalling.failed", urlValue));
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage("catch.exception"), ex);
            return initialContext;
        }

        // This must be made with the xml file has an element root
        // Log level Fine Unmarshalling ok       
        OeContextComplexType oeContext = root.getValue();
        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("context.binding.ok"));

        /* OeContext contains the complete context */
        /* I create a map with the datasourcePool Name as key and datasourcePool as Value
         * This will be useful to instanciate the db connector later.
         */
        List<DataSourcePoolPropertiesComplexType> dataSourcePoolList = oeContext.getDataSourcePoolProperties();
        int listSize = dataSourcePoolList.size();
        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("number.dataSourcePoolProperties.found",
                listSize));

        //Loop on dataSourcePoolList iterator
        for (DataSourcePoolPropertiesComplexType dspComplexType : dataSourcePoolList) {
            mDSPMap.put(dspComplexType.getDbConnectorName(), dspComplexType);
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("datasourcepoolproperties.found.in.context",
                    dspComplexType.getDbConnectorName()));
        }

        // Now Let's read JdbcResource
        List<JdbcResourceComplexType> jdbcResourceList = oeContext.getJdbcResources();
        listSize = jdbcResourceList.size();
        
        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("number.jdbcResource.declaration.found",
                listSize));

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
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("in.process"));


            /* Check if this JNDI name is already in the context. In that case the 
             * second instance is not taken into account 
             */
            try {
                initialContext.lookup(jndiName);
                LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("jndi.value.already.defined", jndiName));
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
            if (dspProperties.getResourceType().equals(InitialContexFactoryImpl.DATASOURCE_TYPE)) {
                LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("datasource.in.process", jndiName));
                DataSource dataSource = mDSPFactory.getDataSource(dspProperties);
                /* Check if datasource is not null then put in the context since exception are catch */
                if (null != dataSource) {
                    datasourceMap.put(dbConnectorName, dataSource);
                    try {
                        initialContext.rebind(jndiName, dataSource);
                    } catch (NamingException ex) {
                        initialContext.bind(jndiName, dataSource);
                    }
                    LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("datasource.processed.bind.success", jndiName));
                }

            } else if (dspProperties.getResourceType()
                    .equals(InitialContexFactoryImpl.XADATASOURCE_TYPE)) {
                LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("xadatasource.in.process", jndiName));
                XADataSource xaDataSource = mDSPFactory.getXADataSource(dspProperties);
                if (null != xaDataSource) {
                    /* Store the XAdatasource in a map for reusing purpose see above */
                    datasourceMap.put(dbConnectorName, (DataSource) xaDataSource);
                    initialContext.rebind(jndiName, xaDataSource);
                    LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("xadatasource.processed.bind.success", jndiName));
                }
            } else {
                LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("bad.resource.type", 
                        dspProperties.getResourceType(), dspProperties.getDatabaseName()));
            }
        }

        /* the context contains the binding and the datasource or xaDatasource links 
         * Return the context
         */
        return initialContext;
    }
}
