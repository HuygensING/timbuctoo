package nl.knaw.huygens.timbuctoo.remote.rs;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9",
  name = "urlset"
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class UrlsetBean {

  @XmlElement(name = "md", namespace = "http://www.openarchives.org/rs/terms/")
  private RsMdBean rsMd;
  @XmlElement(name = "ln", namespace = "http://www.openarchives.org/rs/terms/")
  private List<RsLnBean> linkList = new ArrayList<>();
  @XmlElement(name = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
  private List<UrlBean> urlList = new ArrayList<>();

  private UrlsetBean() {}

  public UrlsetBean(RsMdBean rsMd) {
    this.rsMd = rsMd;
  }

  public RsMdBean getRsMd() {
    return rsMd;
  }

  public UrlsetBean setRsMd(RsMdBean rsMd) {
    this.rsMd = rsMd;
    return this;
  }

  public List<RsLnBean> getLinkList() {
    return linkList;
  }

  public UrlsetBean setLinkList(List<RsLnBean> linkList) {
    this.linkList = linkList;
    return this;
  }

  public List<UrlBean> getUrlList() {
    return urlList;
  }

  public UrlsetBean setUrlList(List<UrlBean> urlList) {
    this.urlList = urlList;
    return this;
  }

  public UrlsetBean add(RsLnBean rsLn) {
    linkList.add(rsLn);
    return this;
  }

  public UrlsetBean add(UrlBean url) {
    urlList.add(url);
    return this;
  }
}
