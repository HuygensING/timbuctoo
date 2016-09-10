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
  private RsMdBean rsMd;
  @XmlElement(name = "ln", namespace = "http://www.openarchives.org/rs/terms/")
  private List<RsLnBean> linkList = new ArrayList<>();

  public RsMdBean getMetadata() {
    return rsMd;
  }

  public T setMetadata(@Nonnull RsMdBean rsMd) {
    this.rsMd = Preconditions.checkNotNull(rsMd);
    return (T) this;
  }

  public List<RsLnBean> getLinkList() {
    return linkList;
  }

  public abstract List<C> getItemList();

  public T add(C item) {
    getItemList().add(item);
    return (T) this;
  }

  public T add(RsLnBean rsLn) {
    linkList.add(rsLn);
    return (T) this;
  }

}
