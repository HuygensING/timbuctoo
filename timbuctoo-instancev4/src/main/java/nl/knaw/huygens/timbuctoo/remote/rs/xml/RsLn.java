package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@XmlType(name = "ln",
  namespace = "http://www.openarchives.org/rs/terms/",
  propOrder = { "rel", "href" }
  )
@XmlAccessorType(XmlAccessType.FIELD)
public class RsLn {

  @XmlAttribute() private String rel;
  @XmlAttribute() private String href;

  @XmlAttribute() private String encoding;
  @XmlAttribute() private String hash;
  @XmlAttribute() private Long length;
  @XmlAttribute() private ZonedDateTime modified;
  @XmlAttribute() private String path;
  @XmlAttribute() private Integer pri;
  @XmlAttribute() private String type;

  private RsLn() {}

  public RsLn(@Nonnull String rel, @Nonnull String href) {
    withRel(rel).withHref(href);
  }

  public String getRel() {
    return rel;
  }

  public RsLn withRel(@Nonnull String rel) {
    this.rel = Objects.requireNonNull(rel);
    return this;
  }

  public String getHref() {
    return href;
  }

  public RsLn withHref(@Nonnull String href) {
    this.href = Objects.requireNonNull(href);
    return this;
  }

  public Optional<String> getEncoding() {
    return Optional.ofNullable(encoding);
  }

  public RsLn withEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public Optional<String> getHash() {
    return Optional.ofNullable(hash);
  }

  public RsLn withHash(String hash) {
    this.hash = hash;
    return this;
  }

  public Optional<Long> getLength() {
    return Optional.ofNullable(length);
  }

  public RsLn withLength(Long length) {
    this.length = length;
    return this;
  }

  public Optional<ZonedDateTime> getModified() {
    return Optional.ofNullable(modified);
  }

  public RsLn withModified(ZonedDateTime modified) {
    this.modified = modified;
    return this;
  }

  public Optional<String> getPath() {
    return Optional.ofNullable(path);
  }

  public RsLn withPath(String path) {
    this.path = path;
    return this;
  }

  public Optional<Integer> getPri() {
    return Optional.ofNullable(pri);
  }

  public RsLn withPri(Integer pri) {
    this.pri = pri;
    return this;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public RsLn withType(String type) {
    this.type = type;
    return this;
  }
}
