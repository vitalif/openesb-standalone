package net.openesb.standalone.naming.jndi;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import net.openesb.standalone.oecontext.binding.DataSourcePoolPropertiesComplexType;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public interface DataSourcePoolFactory {
    
    public DataSource getDataSource (DataSourcePoolPropertiesComplexType dSPProperties) ;    
    public XADataSource getXADataSource (DataSourcePoolPropertiesComplexType dSPProperties) ;
                   
}
