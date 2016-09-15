package nl.knaw.huygens.timbuctoo.remote.rs.xml;


import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  name = "urlset"
  )
public class Urlset extends RsRoot<Urlset, UrlItem> {

  public static final QName QNAME = new QName("http://www.sitemaps.org/schemas/sitemap/0.9", "urlset");

  @XmlElement(name = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private List<UrlItem> urlList = new ArrayList<>();

  protected Urlset() {}

  public Urlset(@Nonnull RsMd rsMd) {
    withMetadata(rsMd);
  }

  public List<UrlItem> getItemList() {
    return urlList;
  }

}
