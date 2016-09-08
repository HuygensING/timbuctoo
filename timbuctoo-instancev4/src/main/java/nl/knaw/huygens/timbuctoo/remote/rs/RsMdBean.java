package nl.knaw.huygens.timbuctoo.remote.rs;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;

@XmlType(name = "md",
  namespace = "http://www.openarchives.org/rs/terms/"
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class RsMdBean {

  @XmlAttribute() private ZonedDateTime at;
  @XmlAttribute() private String capability;
  @XmlAttribute() private ZonedDateTime completed;
  @XmlAttribute() private ZonedDateTime from;
  @XmlAttribute() private ZonedDateTime until;

  @XmlAttribute() private String change;
  @XmlAttribute() private String encoding;
  @XmlAttribute() private String hash;
  @XmlAttribute() private Long length;
  @XmlAttribute() private String path;
  @XmlAttribute() private String type;

  private RsMdBean() {}

  public RsMdBean(String capability) {
    this.capability = capability;
  }

  public ZonedDateTime getAt() {
    return at;
  }

  public RsMdBean setAt(ZonedDateTime at) {
    this.at = at;
    return this;
  }

  public String getCapability() {
    return capability;
  }

  public RsMdBean setCapability(String capability) {
    this.capability = capability;
    return this;
  }

  public ZonedDateTime getCompleted() {
    return completed;
  }

  public RsMdBean setCompleted(ZonedDateTime completed) {
    this.completed = completed;
    return this;
  }

  public ZonedDateTime getFrom() {
    return from;
  }

  public RsMdBean setFrom(ZonedDateTime from) {
    this.from = from;
    return this;
  }

  public ZonedDateTime getUntil() {
    return until;
  }

  public RsMdBean setUntil(ZonedDateTime until) {
    this.until = until;
    return this;
  }

  public String getChange() {
    return change;
  }

  public RsMdBean setChange(String change) {
    this.change = change;
    return this;
  }

  public String getEncoding() {
    return encoding;
  }

  public RsMdBean setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public String getHash() {
    return hash;
  }

  public RsMdBean setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Long getLength() {
    return length;
  }

  public RsMdBean setLength(Long length) {
    this.length = length;
    return this;
  }

  public String getPath() {
    return path;
  }

  public RsMdBean setPath(String path) {
    this.path = path;
    return this;
  }

  public String getType() {
    return type;
  }

  public RsMdBean setType(String type) {
    this.type = type;
    return this;
  }
}
