package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

class CapabilityList {

  private final File file;
  private final Document doc;
  private final Node root;

  CapabilityList(File file, File sourceDescription) throws ResourceSyncException {
    this.file = file;

    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      if (file.exists() && file.length() > 0) {
        doc = docBuilder.parse(file);
        root = doc.getFirstChild();
      } else {
        doc = docBuilder.newDocument();
        root = createRootNode(doc);
        doc.appendChild(root);
      }
      updateMetaData(root, doc, sourceDescription);
      saveDocument(doc, file);
    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
      throw new ResourceSyncException(e);
    }
  }

  private void saveDocument(Document doc, File file) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(file);

    transformer.transform(source, result);
  }

  private Element createRootNode(Document doc) {
    Element urlSet = doc.createElement("urlset");
    urlSet.setAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
    urlSet.setAttribute("xmlns:rs", "http://www.openarchives.org/rs/terms/");
    return urlSet;
  }

  private void updateMetaData(Node root, Document doc, File sourceDescription) {
    if (root.getChildNodes().getLength() == 0) {
      Element upLink = doc.createElement("rs:ln");
      upLink.setAttribute("rel", "up");
      upLink.setAttribute("href", sourceDescription.getPath());
      root.appendChild(upLink);

      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", "capabilitylist");
      root.appendChild(metaData);
    }
  }

  void addResourceList(File resourceListFile) throws ResourceSyncException {
    Element url = doc.createElement("url");

    Element loc = doc.createElement("loc");
    loc.appendChild(doc.createTextNode(resourceListFile.getPath()));
    url.appendChild(loc);

    Element metaData = doc.createElement("rs:md");
    metaData.setAttribute("capability", "resourcelist");
    url.appendChild(metaData);

    root.appendChild(url);

    try {
      saveDocument(doc, file);
    } catch (TransformerException e) {
      throw new ResourceSyncException(e);
    }
  }

}
