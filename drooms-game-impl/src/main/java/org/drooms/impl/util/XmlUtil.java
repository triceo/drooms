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
     *            The XML string to process.
     * @return Pretty-printed XML string.
     */
    public static final String prettyPrint(final String xmlString) {
        try (InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = documentBuilder.parse(inputStream);

            final TransformerFactory tfactory = TransformerFactory.newInstance();
            final Transformer serializer = tfactory.newTransformer();
            // Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            final DOMSource xmlSource = new DOMSource(document);
            final StreamResult outputTarget = new StreamResult(baos);
            serializer.transform(xmlSource, outputTarget);
            return baos.toString("utf-8");
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException ex) {
            throw new RuntimeException("Can't pretty print xml!", ex);
        }
    }
}
