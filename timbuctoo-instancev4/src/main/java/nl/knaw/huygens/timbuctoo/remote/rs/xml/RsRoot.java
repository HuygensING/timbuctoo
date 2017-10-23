package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class RsRoot<T extends RsRoot, C extends RsItem> {

  @XmlElement(name = "md", namespace = "http://www.openarchives.org/rs/terms/")
  private RsMd rsMd;
  @XmlElement(name = "ln", namespace = "http://www.openarchives.org/rs/terms/")
  private List<RsLn> linkList = new ArrayList<>();

  public RsMd getMetadata() {
    return rsMd;
  }

  public T withMetadata(@Nonnull RsMd rsMd) {
    this.rsMd = Objects.requireNonNull(rsMd);
    return (T) this;
  }

  public List<RsLn> getLinkList() {
    return linkList;
  }

  public abstract List<C> getItemList();

  public T addItem(C item) {
    getItemList().add(item);
    return (T) this;
  }

  public T addLink(RsLn rsLn) {
    linkList.add(rsLn);
    return (T) this;
  }

  public String getLink(String rel) {
    for (RsLn rsLn : linkList) {
      if (rel.equalsIgnoreCase(rsLn.getRel())) {
        return rsLn.getHref();
      }
    }
    return null;
  }

  public int getLevel() {
    return Capability.levelfor(rsMd.getCapability().orElse(""));
  }

  public Optional<Capability> getCapability() {
    try {
      return Optional.of(Capability.forString(rsMd.getCapability().orElse("")));
    } catch (IllegalArgumentException e) {
      return Optional.ofNullable(null);
    }
  }

}
