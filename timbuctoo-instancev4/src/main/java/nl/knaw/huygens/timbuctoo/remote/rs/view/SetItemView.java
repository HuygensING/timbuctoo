package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;


/**
 * Render a RsItem, a &lt;url&gt; or &lt;sitemap&gt; element.
 */
public class SetItemView {

  private String name;
  private String location;
  private String capability;
  private String describedBy;

  public SetItemView(RsItem<?> rsItem) {
    init(rsItem, new Interpreter() {});
  }

  public SetItemView(RsItem<?> rsItem, Interpreter interpreter) {
    init(rsItem, interpreter);
  }

  private void init(RsItem<?> rsItem, Interpreter interpreter) {

    location = rsItem.getLoc();
    name = interpreter.getItemNameInterpreter().apply(rsItem);
    // a nullItemNameInterpreter as default interpreter not allowed (Function<RsItem, String> returning null).
    if (location.equals(name)) {
      name = null;
    }

    capability = rsItem.getMetadata()
      .flatMap(RsMd::getCapability)
      .orElse(null);

    describedBy = rsItem.getLinkList().stream()
      .filter(rsLn -> "describedby".equals(rsLn.getRel()))
      .findAny()
      .map(RsLn::getHref)
      .orElse(null);
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getName() {
    return name;
  }

  public String getLocation() {
    return location;
  }

  public String getCapability() {
    return capability;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getDescribedBy() {
    return describedBy;
  }
}
