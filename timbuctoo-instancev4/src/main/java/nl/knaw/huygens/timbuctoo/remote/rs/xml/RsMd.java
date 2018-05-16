package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@XmlType(name = "md",
  namespace = "http://www.openarchives.org/rs/terms/"
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class RsMd {

  @XmlAttribute() private String capability;

  @XmlAttribute() private ZonedDateTime at;
  @XmlAttribute() private ZonedDateTime datetime;
  @XmlAttribute() private ZonedDateTime completed;
  @XmlAttribute() private ZonedDateTime from;
  @XmlAttribute() private ZonedDateTime until;

  @XmlAttribute() private String change;
  @XmlAttribute() private String encoding;
  @XmlAttribute() private String hash;
  @XmlAttribute() private Long length;
  @XmlAttribute() private String path;
  @XmlAttribute() private String type;

  public RsMd() {}

  public RsMd(String capability) {
    this.capability = Objects.requireNonNull(capability);
  }

  public Optional<String> getCapability() {
    return Optional.ofNullable(capability);
  }

  public RsMd withCapability(String capability) {
    this.capability = capability;
    return this;
  }

  public Optional<ZonedDateTime> getAt() {
    return Optional.ofNullable(at);
  }

  public RsMd withAt(ZonedDateTime at) {
    this.at = at;
    return this;
  }

  public Optional<ZonedDateTime> getDateTime() {
    return Optional.ofNullable(datetime);
  }

  public RsMd withDateTime(ZonedDateTime datetime) {
    this.datetime = datetime;
    return this;
  }

  public Optional<ZonedDateTime> getCompleted() {
    return Optional.ofNullable(completed);
  }

  public RsMd withCompleted(ZonedDateTime completed) {
    this.completed = completed;
    return this;
  }

  public Optional<ZonedDateTime> getFrom() {
    return Optional.ofNullable(from);
  }

  public RsMd withFrom(ZonedDateTime from) {
    this.from = from;
    return this;
  }

  public Optional<ZonedDateTime> getUntil() {
    return Optional.ofNullable(until);
  }

  public RsMd withUntil(ZonedDateTime until) {
    this.until = until;
    return this;
  }

  public Optional<String> getChange() {
    return Optional.ofNullable(change);
  }

  public RsMd withChange(String change) {
    this.change = change;
    return this;
  }

  public Optional<String> getEncoding() {
    return Optional.ofNullable(encoding);
  }

  public RsMd withEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public Optional<String> getHash() {
    return Optional.ofNullable(hash);
  }

  public RsMd withHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Optional<Long> getLength() {
    return Optional.ofNullable(length);
  }

  public RsMd withLength(Long length) {
    this.length = length;
    return this;
  }

  public Optional<String> getPath() {
    return Optional.ofNullable(path);
  }

  public RsMd withPath(String path) {
    this.path = path;
    return this;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public RsMd withType(String type) {
    this.type = type;
    return this;
  }
}
