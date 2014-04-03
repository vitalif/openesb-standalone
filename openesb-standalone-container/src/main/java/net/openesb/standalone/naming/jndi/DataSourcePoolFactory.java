package net.openesb.standalone.naming.jndi;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import net.openesb.standalone.naming.jaxb.DataSourcePoolProperties;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public interface DataSourcePoolFactory {
    
    public DataSource getDataSource (DataSourcePoolProperties dSPProperties) ;    
    public XADataSource getXADataSource (DataSourcePoolProperties dSPProperties) ;
                   
}
