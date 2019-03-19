package no.nav.syfo.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;

@Slf4j
public class XmlUtil {

    public static Document parseXml(String xml) {
        return parseXml(xml, true);
    }

    private static Document parseXml(String xml, boolean namespaceAware) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(namespaceAware); // needed for use of NamespaceContext in XPath
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Feil i parsing av XML", e);
        }
        return null;
    }


    public static String xmlTilHtml(String xml, InputStream xslPath) throws JAXBException, FileNotFoundException, TransformerException {
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

