package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "urlset")
public class UrlSet {
  private Metadata metadata;
  private Link parent;
  private Link description;
  private List<UrlItem> urlList = new ArrayList<>();

  public UrlSet() {
  }

  @JacksonXmlProperty(localName = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public List<UrlItem> getItemList() {
    return urlList;
  }

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public void setItemList(List<UrlItem> urlList) {
    this.urlList = urlList;
  }

  @JacksonXmlProperty(localName = "capability", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9", isAttribute = true)
  public Metadata getMetadata() {
    return metadata;
  }

  @JacksonXmlProperty(localName = "md", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  @JacksonXmlProperty(localName = "ln", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public Link getParent() {
    return parent;
  }

  @JacksonXmlProperty(localName = "ln", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public void setParent(Link parent) {
    this.parent = parent;
  }

  @JacksonXmlProperty(localName = "describedBy", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public Link getDescription() {
    return description;
  }

  @JacksonXmlProperty(localName = "describedBy", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  public void setDescription(Link description) {
    this.description = description;
  }
}
