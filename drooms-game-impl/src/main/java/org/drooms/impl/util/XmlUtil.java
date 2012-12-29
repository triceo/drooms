package org.drooms.impl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtil {
    /**
     * Makes the specified XML string pretty.
     * 
     * @param xmlString
     * @return
     */
    public static final String prettyPrint(final String xmlString) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
            Document document = documentBuilder.parse(inputStream);

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer serializer;
            serializer = tfactory.newTransformer();
            // Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource xmlSource = new DOMSource(document);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult outputTarget = new StreamResult(baos);
            serializer.transform(xmlSource, outputTarget);
            return baos.toString("utf-8");
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException ex) {
            throw new RuntimeException("Can't pretty print xml!", ex);
        }
    }
}
