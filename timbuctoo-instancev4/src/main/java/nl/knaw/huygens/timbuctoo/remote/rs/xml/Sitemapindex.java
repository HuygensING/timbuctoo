package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  name = "sitemapindex"
  )
public class Sitemapindex extends RsRoot<Sitemapindex, SitemapItem> {

  public static final QName QNAME = new QName("http://www.sitemaps.org/schemas/sitemap/0.9", "sitemapindex");

  @XmlElement(name = "sitemap", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private List<SitemapItem> siteMapList = new ArrayList<>();

  protected Sitemapindex() {}

  public Sitemapindex(@Nonnull RsMd rsMd) {
    withMetadata(rsMd);
  }

  public List<SitemapItem> getItemList() {
    return siteMapList;
  }

}
