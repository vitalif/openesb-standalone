package net.openesb.standalone.naming.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import net.openesb.standalone.naming.jndi.impl.InitialContexFactoryImpl;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

/**
 *
 * @author Paul PEREZ (paul.perez at pymma.com)
 * @author OpenESB Community
 */
public class TestConnection01 {

    public static void main(String[] args) throws Exception{
        InitialContextFactory factory = new InitialContexFactoryImpl();
        Hashtable environment = new Hashtable();
        environment.put(Context.PROVIDER_URL, "file:///G:/projects/jndi-standalone/jndi-standalone/src/main/resources/net/openesb/standalone/naming/utils/OEContextSample.xml");

        Context context = null;
        try {
            context = factory.getInitialContext(environment);
        } catch (NamingException ex) {
            Logger.getLogger(TestConnection01.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Get initial context Error");
            System.exit(1);
        }

        DataSource dsMySQL = null;
        try {
            dsMySQL = (DataSource) context.lookup("MySQLServer01");
        } catch (NamingException ex) {
            Logger.getLogger(TestConnection01.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Look up Error");
            System.exit(1);
        }
        Connection connectionMySQL = null;
        try {
            connectionMySQL = dsMySQL.getConnection();
            PreparedStatement sta2 = connectionMySQL.prepareStatement("SELECT * FROM test.test");
            ResultSet rs = sta2.executeQuery();
            rs.next();
            String name = rs.getString("NAME");
            System.out.println(name);
        } catch (SQLException ex) {
            Logger.getLogger(TestConnection01.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Look up Error");
            System.exit(1);
        }


        // Derby test
        DataSource dsDerby = (DataSource)context.lookup("Derby01"); 
        Connection connectionDerby = dsDerby.getConnection();
        PreparedStatement staDerby = connectionDerby.prepareStatement("select * from APP.CUSTOMER where CUSTOMER_ID=1");
        ResultSet rsDerby = staDerby.executeQuery();
        rsDerby.next();
        String customerID = rsDerby.getString("NAME");
        System.out.println(customerID);
        
        //PostGresql test
        DataSource dsPostgres = (DataSource)context.lookup("Postgres01");
        Connection connectionPG = dsPostgres.getConnection();
        PreparedStatement staPG = connectionPG.prepareStatement("SELECT *  FROM public.distributors");
        ResultSet rsPG = staPG.executeQuery();
        rsPG.next();
        String customerName = rsPG.getString(2);
        System.out.println(customerName);

    }
}
