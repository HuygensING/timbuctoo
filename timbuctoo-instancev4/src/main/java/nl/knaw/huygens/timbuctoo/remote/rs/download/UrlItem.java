package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.slf4j.LoggerFactory;

public class UrlItem {
  private String loc;
  @JacksonXmlProperty(localName = "md", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private Metadata metadata;

  public String getLoc() {
    return loc;
  }

  public void setLoc(String loc) {
    this.loc = loc;
  }

  public Metadata getMetadata() {
    LoggerFactory.getLogger(UrlItem.class).info("getMetaData");
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }
}
