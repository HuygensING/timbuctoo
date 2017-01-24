package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

  public ObjectFactory() {}

  public Urlset createUrlset() {
    return new Urlset();
  }

  @XmlElementDecl(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9", name = "urlset")
  public JAXBElement<Urlset> createUrlset(Urlset value) {
    return new JAXBElement<Urlset>(Urlset.QNAME, Urlset.class, null, value);
  }

  public Sitemapindex createSitemapIndex() {
    return new Sitemapindex();
  }

  @XmlElementDecl(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9", name = "sitemapindex")
  public JAXBElement<Sitemapindex> createSitemapIndex(Sitemapindex value) {
    return new JAXBElement<Sitemapindex>(Sitemapindex.QNAME, Sitemapindex.class, null, value);
  }
}
