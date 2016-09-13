package nl.knaw.huygens.timbuctoo.remote.rs;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class RsRoot<T extends RsRoot, C extends RsItem> {

  @XmlElement(name = "md", namespace = "http://www.openarchives.org/rs/terms/")
  private RsMd rsMd;
  @XmlElement(name = "ln", namespace = "http://www.openarchives.org/rs/terms/")
  private List<RsLn> linkList = new ArrayList<>();

  public RsMd getMetadata() {
    return rsMd;
  }

  public T setMetadata(@Nonnull RsMd rsMd) {
    this.rsMd = Preconditions.checkNotNull(rsMd);
    return (T) this;
  }

  public List<RsLn> getLinkList() {
    return linkList;
  }

  public abstract List<C> getItemList();

  public T add(C item) {
    getItemList().add(item);
    return (T) this;
  }

  public T add(RsLn rsLn) {
    linkList.add(rsLn);
    return (T) this;
  }

}
