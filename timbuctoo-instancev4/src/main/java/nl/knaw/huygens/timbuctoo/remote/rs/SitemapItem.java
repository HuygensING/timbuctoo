package nl.knaw.huygens.timbuctoo.remote.rs;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "sitemap",
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9"
  )
public class SitemapItem extends RsItem<SitemapItem> {

  private SitemapItem() {}

  public SitemapItem(@Nonnull String loc) {
    setLoc(loc);
  }

}
