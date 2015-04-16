package net.openesb.standalone.naming.jndi;

import net.openesb.standalone.naming.jndi.ds.DataSourcePoolFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.naming.jaxb.DataSourcePoolProperties;
import net.openesb.standalone.naming.jaxb.JDBCResource;
import net.openesb.standalone.naming.jndi.ds.tomcat.TomcatDataSourcePoolFactory;
import net.openesb.standalone.utils.I18NBundle;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class InitialContexFactoryImpl extends AbstractContextFactory implements InitialContextFactory {

    private static final Logger LOG = Logger.getLogger(InitialContexFactoryImpl.class.getName());
    public static final String DATASOURCE_TYPE = "Datasource";
    public static final String XADATASOURCE_TYPE = "XADatasource";
    private final Map<String, DataSourcePoolProperties> mDSPMap = new HashMap<String, DataSourcePoolProperties>();

    private final DataSourcePoolFactory mDSPFactory = new TomcatDataSourcePoolFactory();

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Context namingContext = getContext();
        
        String urlValue = (String) environment.get(Context.PROVIDER_URL);
        if (urlValue == null) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_NO_CONTEXT_URL));
        } else {
            Set<net.openesb.standalone.naming.jaxb.Context> contexts = loadContexts(urlValue);
            for (net.openesb.standalone.naming.jaxb.Context context : contexts) {
                try {
                    populate(context, namingContext);
                } catch (NamingException ne) {
                    LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                            LocalStringKeys.NAMING_CONTEXT_BIND_FAILURE));
                }
            }
        }

        return namingContext;
    }
    
    private Context getContext() throws NamingException {
        /*Context initialisation Just set the system properties and  use the class InitialContext*/
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        return new InitialContext();
    }

    private void populate(net.openesb.standalone.naming.jaxb.Context context, Context namingContext) throws NamingException {
        /* OeContext contains the complete context */
        /* I create a map with the datasourcePool Name as key and datasourcePool as Value
         * This will be useful to instanciate the db connector later.
         */
        List<DataSourcePoolProperties> dataSourcePoolList = context.getDataSourcePoolProperties();
        int listSize = dataSourcePoolList.size();
        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("number.dataSourcePoolProperties.found",
                listSize));

        //Loop on dataSourcePoolList iterator
        for (DataSourcePoolProperties dspComplexType : dataSourcePoolList) {
            mDSPMap.put(dspComplexType.getDbConnectorName(), dspComplexType);
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("datasourcepoolproperties.found.in.context",
                    dspComplexType.getDbConnectorName()));
        }

        // Now Let's read JdbcResource
        List<JDBCResource> jdbcResourceList = context.getJdbcResources();
        listSize = jdbcResourceList.size();

        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("number.jdbcResource.declaration.found",
                listSize));

        //Loop on JDBCResourceList iterator
        for (JDBCResource jdbcResource : jdbcResourceList) {
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
                namingContext.lookup(jndiName);
                LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("jndi.value.already.defined", jndiName));
                continue;
            } catch (NamingException ex) {
                // Nothing else to do. Having an exception is the normal process
            }

            Map<String, DataSource> datasourceMap = new HashMap<String, DataSource>();

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
                    namingContext.rebind(jndiName, (XADataSource) datasourceMap.get(dbConnectorName));
                } else {
                    namingContext.rebind(jndiName, datasourceMap.get(dbConnectorName));
                }
                continue;
            }

            // Retrieve DataSourcePoolPropertie
            DataSourcePoolProperties dspProperties = mDSPMap.get(dbConnectorName);
            // Check if Datasourse or XA Datasource
            if (dspProperties != null) {
                if (dspProperties.getResourceType().equals(InitialContexFactoryImpl.DATASOURCE_TYPE)) {
                    LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("datasource.in.process", jndiName));
                    DataSource dataSource = mDSPFactory.getDataSource(dspProperties);
                    /* Check if datasource is not null then put in the context since exception are catch */
                    if (null != dataSource) {
                        datasourceMap.put(dbConnectorName, dataSource);
                        try {
                            namingContext.rebind(jndiName, dataSource);
                        } catch (NamingException ex) {
                            namingContext.bind(jndiName, dataSource);
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
                        namingContext.rebind(jndiName, xaDataSource);
                        LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("xadatasource.processed.bind.success", jndiName));
                    }
                } else {
                    LOG.log(Level.FINE, I18NBundle.getBundle().getMessage("bad.resource.type",
                            dspProperties.getResourceType(), dspProperties.getDatabaseName()));
                }
            }
        }
    }
}
