package nl.knaw.huygens.timbuctoo.remote.rs;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "url",
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9"
  )
public class UrlItem extends RsItem<UrlItem> {

  private UrlItem() {}

  public UrlItem(@Nonnull String loc) {
    setLoc(loc);
  }

}
