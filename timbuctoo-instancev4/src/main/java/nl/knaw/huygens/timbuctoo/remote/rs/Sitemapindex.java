package nl.knaw.huygens.timbuctoo.remote.rs;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  name = "sitemapindex"
  )
public class Sitemapindex extends RsRoot<Sitemapindex, SitemapItem> {

  @XmlElement(name = "sitemap", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private List<SitemapItem> siteMapList = new ArrayList<>();

  private Sitemapindex() {}

  public Sitemapindex(@Nonnull RsMdBean rsMd) {
    setMetadata(rsMd);
  }

  public List<SitemapItem> getItemList() {
    return siteMapList;
  }

}
