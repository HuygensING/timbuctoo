package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
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

class FileSystemResourceList implements ResourceList {
  private final File resourceList;
  private final ResourceSyncDateFormatter resourceSyncDateFormatter;

  public FileSystemResourceList(File resourceList, ResourceSyncDateFormatter resourceSyncDateFormatter) {
    this.resourceList = resourceList;
    this.resourceSyncDateFormatter = resourceSyncDateFormatter;
  }

  @Override
  public void addFile(CachedFile fileToAdd) {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    Node root;
    Document doc;
    if (!resourceList.exists()) {
      doc = docBuilder.newDocument();
      root = createRootNode(doc);
      doc.appendChild(root);
    } else {
      try {
        doc = docBuilder.parse(resourceList);

      } catch (SAXException | IOException e) {
        throw new RuntimeException(e);
      }
      root = doc.getFirstChild();
    }

    updateMetaData(root, doc);

    root.appendChild(createUrlNode(doc, fileToAdd));

    try {
      saveDocument(doc);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }


  private void saveDocument(Document doc) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(resourceList);

    transformer.transform(source, result);
  }

  private Element createUrlNode(Document doc, CachedFile fileToAdd) {
    Element url = doc.createElement("url");
    Element loc = doc.createElement("loc");
    loc.appendChild(doc.createTextNode(fileToAdd.getFile().getPath()));

    url.appendChild(loc);
    return url;
  }

  private void updateMetaData(Node root, Document doc) {
    if (root.getChildNodes().getLength() == 0) {
      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("at", resourceSyncDateFormatter.now());
      metaData.setAttribute("capability", "resourcelist");
      root.appendChild(metaData);
    } else {
      root.getFirstChild().getAttributes().getNamedItem("at").setNodeValue(resourceSyncDateFormatter.now());
    }
  }

  private Element createRootNode(Document doc) {
    Element urlSet = doc.createElement("urlset");
    urlSet.setAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
    urlSet.setAttribute("xmlns:rs", "http://www.openarchives.org/rs/terms/");
    return urlSet;
  }
}
