package nl.knaw.huygens.timbuctoo.remote.rs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "url",
  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9"
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class UrlBean {

  private String loc;
  private ZonedDateTime lastmod;
  private String changefreq;
  @XmlElement(name = "md", namespace = "http://www.openarchives.org/rs/terms/")
  private RsMdBean rsMd;
  @XmlElement(name = "ln", namespace = "http://www.openarchives.org/rs/terms/")
  private List<RsLnBean> rsLnList = new ArrayList<>();

  private UrlBean() {}

  public UrlBean(String loc) {
    this.loc = loc;
  }

  public String getLoc() {
    return loc;
  }

  public UrlBean setLoc(String loc) {
    this.loc = loc;
    return this;
  }

  public ZonedDateTime getLastmod() {
    return lastmod;
  }

  public UrlBean setLastmod(ZonedDateTime lastmod) {
    this.lastmod = lastmod;
    return this;
  }

  public String getChangefreq() {
    return changefreq;
  }

  public UrlBean setChangefreq(String changefreq) {
    this.changefreq = changefreq;
    return this;
  }

  public RsMdBean getRsMd() {
    return rsMd;
  }

  public UrlBean setRsMd(RsMdBean rsMd) {
    this.rsMd = rsMd;
    return this;
  }

  public List<RsLnBean> getRsLnList() {
    return rsLnList;
  }

  public UrlBean setRsLnList(List<RsLnBean> rsLnList) {
    this.rsLnList = rsLnList;
    return this;
  }

  public UrlBean add(RsLnBean rsLn) {
    rsLnList.add(rsLn);
    return this;
  }
}
