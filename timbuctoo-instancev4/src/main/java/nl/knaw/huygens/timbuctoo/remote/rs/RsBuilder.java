package nl.knaw.huygens.timbuctoo.remote.rs;


import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;

public class RsBuilder {

  private final ResourceSyncContext rsContext;

  private File file;
  private InputSource inputSource;
  private InputStream inputStream;
  private Node node;
  private Reader reader;
  private Source source;
  private URL url;
  private XMLEventReader xmlEventReader;
  private XMLStreamReader xmlStreamReader;

  private QName latestQName;
  private Urlset urlset;
  private Sitemapindex sitemapindex;
  private Throwable error;

  public RsBuilder(ResourceSyncContext rsContext) {
    this.rsContext = rsContext;
  }

  public Optional<RsRoot> build() {
    latestQName = null;
    urlset = null;
    sitemapindex = null;
    error = null;

    JAXBElement<RsRoot> je = null;
    RsRoot rsRoot = null;
    try {
      Unmarshaller unmarshaller = rsContext.createUnmarshaller();
      if (file != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(file);
        file = null;
      } else if (inputSource != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(inputSource);
        inputSource = null;
      } else if (inputStream != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(inputStream);
        inputStream = null;
      } else if (node != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(node);
        node = null;
      } else if (reader != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(reader);
        reader = null;
      } else if (source != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(source);
        source = null;
      } else if (url != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(url);
        url = null;
      } else if (xmlEventReader != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(xmlEventReader);
        xmlEventReader = null;
      } else if (xmlStreamReader != null) {
        je = (JAXBElement<RsRoot>) unmarshaller.unmarshal(xmlStreamReader);
        xmlStreamReader = null;
      }
    } catch (JAXBException e) {
      error = e;
    }

    if (je != null) {
      latestQName = je.getName();
      rsRoot = je.getValue();
      if (latestQName.equals(Urlset.QNAME)) {
        urlset = (Urlset) rsRoot;
      } else if (latestQName.equals(Sitemapindex.QNAME)) {
        sitemapindex = (Sitemapindex) rsRoot;
      }
    }
    return Optional.ofNullable(rsRoot);
  }

  public Optional<QName> getQName() {
    return Optional.ofNullable(latestQName);
  }

  public Optional<Urlset> getUrlset() {
    return Optional.ofNullable(urlset);
  }

  public Optional<Sitemapindex> getSitemapindex() {
    return Optional.ofNullable(sitemapindex);
  }

  public Optional<Throwable> getError() {
    return  Optional.ofNullable(error);
  }

  public RsBuilder setFile(File file) {
    this.file = file;
    return this;
  }

  public RsBuilder setInputSource(InputSource inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  public RsBuilder setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  public RsBuilder setNode(Node node) {
    this.node = node;
    return this;
  }

  public RsBuilder setReader(Reader reader) {
    this.reader = reader;
    return this;
  }

  public RsBuilder setSource(Source source) {
    this.source = source;
    return this;
  }

  public RsBuilder setUrl(URL url) {
    this.url = url;
    return this;
  }

  public RsBuilder setXmlEventReader(XMLEventReader xmlEventReader) {
    this.xmlEventReader = xmlEventReader;
    return this;
  }

  public RsBuilder setXmlStreamReader(XMLStreamReader xmlStreamReader) {
    this.xmlStreamReader = xmlStreamReader;
    return this;
  }
}
