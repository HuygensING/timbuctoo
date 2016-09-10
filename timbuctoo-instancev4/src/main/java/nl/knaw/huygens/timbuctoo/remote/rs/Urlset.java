package nl.knaw.huygens.timbuctoo.remote.rs;


import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  name = "urlset"
  )
public class Urlset extends RsRoot<Urlset, UrlItem> {

  @XmlElement(name = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private List<UrlItem> urlList = new ArrayList<>();

  private Urlset() {}

  public Urlset(@Nonnull RsMdBean rsMd) {
    setMetadata(rsMd);
  }

  public List<UrlItem> getItemList() {
    return urlList;
  }

}
