package net.openesb.standalone.naming.jndi;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import net.openesb.standalone.Constants;
import net.openesb.standalone.LocalStringKeys;
import net.openesb.standalone.naming.jaxb.Context;
import net.openesb.standalone.utils.I18NBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class JAXBContextReader {

    private static final Logger LOG = Logger.getLogger(JAXBContextReader.class.getName());

    private final Unmarshaller unmarshaller;

    public JAXBContextReader() throws Exception {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Looking for the XSD from install root
            String mInstallRoot = System.getProperty(Constants.OPENESB_HOME_PROP);
            String schemaFile = mInstallRoot + File.separatorChar + "config" + File.separatorChar + "context.xsd";
            Schema schema = sf.newSchema(new File(schemaFile));

            JAXBContext jc = JAXBContext.newInstance("net.openesb.standalone.naming.jaxb");

            unmarshaller = jc.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    // Returning false from the handleEvent method will cause the JAXB 
                    // operation to stop
                    return false;
                }
            });
        } catch (SAXException saxe) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_SCHEMA_FAILURE));
            throw saxe;
        } catch (JAXBException jaxbe) {
            LOG.log(Level.SEVERE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_CONTEXT_JAXB_FAILURE));
            throw jaxbe;
        }
    }

    public Context getContext(URL contextUrl) throws Exception {
        Context context = ((JAXBElement<Context>) unmarshaller.unmarshal(
                contextUrl)).getValue();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, I18NBundle.getBundle().getMessage(
                    LocalStringKeys.NAMING_UNMARSHAL_SUCCESS));
        }

        return context;
    }
}
