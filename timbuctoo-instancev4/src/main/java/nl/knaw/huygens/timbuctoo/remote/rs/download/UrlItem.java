package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;
import org.slf4j.LoggerFactory;

public class UrlItem {
  private String loc;
  @JacksonXmlProperty(localName = "md", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private Metadata metadata;
  @JacksonXmlProperty(localName = "ln", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private RsLn rsLn;

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

  public RsLn getLink() {
    LoggerFactory.getLogger(UrlItem.class).info("getLink");
    return rsLn;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }
}
