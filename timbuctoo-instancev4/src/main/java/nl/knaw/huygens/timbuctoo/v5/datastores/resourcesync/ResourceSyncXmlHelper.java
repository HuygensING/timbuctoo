package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.util.Objects;
import java.util.Optional;

public class ResourceSyncXmlHelper {

  public final Document doc;
  private final Node root;
  private final File file;

  ResourceSyncXmlHelper(File file) throws ResourceSyncException {
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

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(file);

      transformer.transform(source, result);
    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
      throw new ResourceSyncException(e);
    }
  }

  void createOrUpdateUplink(String sourceDescriptionUri) {
    Element upLink = getOrCreateElement("rs:ln");
    upLink.setAttribute("rel", "up");
    upLink.setAttribute("href", sourceDescriptionUri);

    Element metaData = getOrCreateMetadataNode();
    root.insertBefore(upLink, metaData);
  }

  private Element createRootNode(Document doc) {
    Element urlSet = doc.createElement("urlset");
    urlSet.setAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
    urlSet.setAttribute("xmlns:rs", "http://www.openarchives.org/rs/terms/");
    return urlSet;
  }

  void addUrlElementWithCapability(String uri, String capability) {
    if (!nodeForUrl(uri).isPresent()) {
      Element url = doc.createElement("url");

      Element loc = doc.createElement("loc");
      loc.appendChild(doc.createTextNode(uri));
      url.appendChild(loc);

      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", capability);
      url.appendChild(metaData);

      root.appendChild(url);
    }
  }

  void addUrlElementWithCapabilityAndDescriptionFile(String uri, String capability, String descriptionFileName) {
    if (!nodeForUrl(uri).isPresent()) {
      Element url = doc.createElement("url");

      Element loc = doc.createElement("loc");
      loc.appendChild(doc.createTextNode(uri));
      url.appendChild(loc);

      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", capability);
      url.appendChild(metaData);

      Element description = doc.createElement("rs:ln");
      description.setAttribute("rel","describedBy");
      description.setAttribute("href", descriptionFileName);
      description.setAttribute("type", "application/rdf+xml");
      url.appendChild(description);

      root.appendChild(url);
    }
  }

  void addUrlElementWithType(String uri, String mimeType) {
    if (!nodeForUrl(uri).isPresent()) {
      Element url = doc.createElement("url");

      Element loc = doc.createElement("loc");
      loc.appendChild(doc.createTextNode(uri));
      url.appendChild(loc);

      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("type", mimeType);
      url.appendChild(metaData);

      root.appendChild(url);
    }
  }

  void addUrlElement(String uri) {
    if (!nodeForUrl(uri).isPresent()) {
      Element url = doc.createElement("url");

      Element loc = doc.createElement("loc");
      loc.appendChild(doc.createTextNode(uri));
      url.appendChild(loc);

      root.appendChild(url);
    }
  }

  void removeUrlElement(String url) {
    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeName().equals("url") && Objects.equals(child.getTextContent(), url)) {
        root.removeChild(child);
        break;
      }
    }
  }

  private Optional<Node> nodeForUrl(String url) {
    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeName().equals("url") && Objects.equals(child.getTextContent(), url)) {

        return Optional.of(child);
      }
    }
    return Optional.empty();
  }

  Element getOrCreateMetadataNode() {
    return getOrCreateElement("rs:md");
  }

  private Element getOrCreateElement(String nodeName) {
    Element metadataNode = null;
    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node node = childNodes.item(i);
      if (node instanceof Element && node.getNodeName().equals(nodeName)) {
        metadataNode = (Element) node;
        break;
      }
    }
    if (metadataNode != null) {
      return metadataNode;
    } else {
      Element metaData = doc.createElement(nodeName);
      if (root.getChildNodes().getLength() == 0) {
        root.appendChild(metaData);
      } else {
        root.insertBefore(metaData, root.getChildNodes().item(0));
      }
      return metaData;
    }
  }

  void save() throws ResourceSyncException {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(file);

      transformer.transform(source, result);
    } catch (TransformerException e) {
      throw new ResourceSyncException(e);
    }

  }

}
