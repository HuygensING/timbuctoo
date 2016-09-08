package nl.knaw.huygens.timbuctoo.remote.rs;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;


@XmlType(name = "ln",
  namespace = "http://www.openarchives.org/rs/terms/",
  propOrder = { "rel", "href" }
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class RsLnBean {

  @XmlAttribute() private String rel;
  @XmlAttribute() private String href;

  @XmlAttribute() private String encoding;
  @XmlAttribute() private String hash;
  @XmlAttribute() private Long length;
  @XmlAttribute() private ZonedDateTime modified;
  @XmlAttribute() private String path;
  @XmlAttribute() private Integer pri;
  @XmlAttribute() private String type;

  private RsLnBean() {}

  public RsLnBean(String rel, String href) {
    this.rel = rel;
    this.href = href;
  }

  public String getRel() {
    return rel;
  }

  public RsLnBean setRel(String rel) {
    this.rel = rel;
    return this;
  }

  public String getHref() {
    return href;
  }

  public RsLnBean setHref(String href) {
    this.href = href;
    return this;
  }

  public String getEncoding() {
    return encoding;
  }

  public RsLnBean setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public String getHash() {
    return hash;
  }

  public RsLnBean setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Long getLength() {
    return length;
  }

  public RsLnBean setLength(Long length) {
    this.length = length;
    return this;
  }

  public ZonedDateTime getModified() {
    return modified;
  }

  public RsLnBean setModified(ZonedDateTime modified) {
    this.modified = modified;
    return this;
  }

  public String getPath() {
    return path;
  }

  public RsLnBean setPath(String path) {
    this.path = path;
    return this;
  }

  public Integer getPri() {
    return pri;
  }

  public RsLnBean setPri(Integer pri) {
    this.pri = pri;
    return this;
  }

  public String getType() {
    return type;
  }

  public RsLnBean setType(String type) {
    this.type = type;
    return this;
  }
}
