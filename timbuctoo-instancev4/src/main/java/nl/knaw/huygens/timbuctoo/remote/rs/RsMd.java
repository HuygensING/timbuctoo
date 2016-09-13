package nl.knaw.huygens.timbuctoo.remote.rs;


import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.Optional;

@XmlType(name = "md",
  namespace = "http://www.openarchives.org/rs/terms/"
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class RsMd {

  @XmlAttribute() private String capability;

  @XmlAttribute() private ZonedDateTime at;
  @XmlAttribute() private ZonedDateTime completed;
  @XmlAttribute() private ZonedDateTime from;
  @XmlAttribute() private ZonedDateTime until;

  @XmlAttribute() private String change;
  @XmlAttribute() private String encoding;
  @XmlAttribute() private String hash;
  @XmlAttribute() private Long length;
  @XmlAttribute() private String path;
  @XmlAttribute() private String type;

  private RsMd() {}

  public RsMd(@Nonnull String capability) {
    this.capability = Preconditions.checkNotNull(capability);
  }

  public String getCapability() {
    return capability;
  }

  public RsMd setCapability(@Nonnull String capability) {
    this.capability = Preconditions.checkNotNull(capability);
    return this;
  }

  public Optional<ZonedDateTime> getAt() {
    return Optional.ofNullable(at);
  }

  public RsMd setAt(ZonedDateTime at) {
    this.at = at;
    return this;
  }

  public Optional<ZonedDateTime> getCompleted() {
    return Optional.ofNullable(completed);
  }

  public RsMd setCompleted(ZonedDateTime completed) {
    this.completed = completed;
    return this;
  }

  public Optional<ZonedDateTime> getFrom() {
    return Optional.ofNullable(from);
  }

  public RsMd setFrom(ZonedDateTime from) {
    this.from = from;
    return this;
  }

  public Optional<ZonedDateTime> getUntil() {
    return Optional.ofNullable(until);
  }

  public RsMd setUntil(ZonedDateTime until) {
    this.until = until;
    return this;
  }

  public Optional<String> getChange() {
    return Optional.ofNullable(change);
  }

  public RsMd setChange(String change) {
    this.change = change;
    return this;
  }

  public Optional<String> getEncoding() {
    return Optional.ofNullable(encoding);
  }

  public RsMd setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public Optional<String> getHash() {
    return Optional.ofNullable(hash);
  }

  public RsMd setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Optional<Long> getLength() {
    return Optional.ofNullable(length);
  }

  public RsMd setLength(Long length) {
    this.length = length;
    return this;
  }

  public Optional<String> getPath() {
    return Optional.ofNullable(path);
  }

  public RsMd setPath(String path) {
    this.path = path;
    return this;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public RsMd setType(String type) {
    this.type = type;
    return this;
  }
}
