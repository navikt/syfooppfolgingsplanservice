package no.nav.syfo.util;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;

public class XmlUtil {

    public static String xmlTilHtml(String xml, InputStream xslPath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

        Source xslDoc = new StreamSource(xslPath);
        Source xmlDoc = new StreamSource(IOUtils.toInputStream(xml, Charset.forName("UTF-8")));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Transformer transformer = transformerFactory.newTransformer(xslDoc);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(xmlDoc, new StreamResult(baos));

        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String xmlTilString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
}

