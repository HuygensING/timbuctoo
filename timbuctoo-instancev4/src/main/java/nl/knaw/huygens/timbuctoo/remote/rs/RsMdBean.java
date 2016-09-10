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
public class RsMdBean {

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

  private RsMdBean() {}

  public RsMdBean(@Nonnull String capability) {
    this.capability = Preconditions.checkNotNull(capability);
  }

  public String getCapability() {
    return capability;
  }

  public RsMdBean setCapability(@Nonnull String capability) {
    this.capability = Preconditions.checkNotNull(capability);
    return this;
  }

  public Optional<ZonedDateTime> getAt() {
    return Optional.ofNullable(at);
  }

  public RsMdBean setAt(ZonedDateTime at) {
    this.at = at;
    return this;
  }

  public Optional<ZonedDateTime> getCompleted() {
    return Optional.ofNullable(completed);
  }

  public RsMdBean setCompleted(ZonedDateTime completed) {
    this.completed = completed;
    return this;
  }

  public Optional<ZonedDateTime> getFrom() {
    return Optional.ofNullable(from);
  }

  public RsMdBean setFrom(ZonedDateTime from) {
    this.from = from;
    return this;
  }

  public Optional<ZonedDateTime> getUntil() {
    return Optional.ofNullable(until);
  }

  public RsMdBean setUntil(ZonedDateTime until) {
    this.until = until;
    return this;
  }

  public Optional<String> getChange() {
    return Optional.ofNullable(change);
  }

  public RsMdBean setChange(String change) {
    this.change = change;
    return this;
  }

  public Optional<String> getEncoding() {
    return Optional.ofNullable(encoding);
  }

  public RsMdBean setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public Optional<String> getHash() {
    return Optional.ofNullable(hash);
  }

  public RsMdBean setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Optional<Long> getLength() {
    return Optional.ofNullable(length);
  }

  public RsMdBean setLength(Long length) {
    this.length = length;
    return this;
  }

  public Optional<String> getPath() {
    return Optional.ofNullable(path);
  }

  public RsMdBean setPath(String path) {
    this.path = path;
    return this;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public RsMdBean setType(String type) {
    this.type = type;
    return this;
  }
}
