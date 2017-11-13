package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Description;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Result;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.ResultIndex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;

import java.net.URI;


/**
 * Render a RsItem, a &lt;url&gt; or &lt;sitemap&gt; element.
 */
public class SetItemView {

  private String location;
  private String capability;
  private ResultView describedBy;

  public SetItemView(ResultIndex resultIndex, RsItem<?> rsItem) {
    init(resultIndex, rsItem, new Interpreter() {});
  }

  public SetItemView(ResultIndex resultIndex, RsItem<?> rsItem, Interpreter interpreter) {
    init(resultIndex, rsItem, interpreter);
  }

  @SuppressWarnings("unchecked")
  private void init(ResultIndex resultIndex, RsItem<?> rsItem, Interpreter interpreter) {

    location = rsItem.getLoc();
    
    capability = rsItem.getMetadata()
      .flatMap(RsMd::getCapability)
      .orElse(null);

    String href = rsItem.getLinkList().stream()
      .filter(rsLn -> "describedBy".equalsIgnoreCase(rsLn.getRel()))
      .findAny()
      .map(RsLn::getHref)
      .orElse(null);
    if (href != null) {
      Result<?> result = resultIndex.getResultMap().get(URI.create(href));
      if (result != null) {
        describedBy = new ResultView(result, interpreter);
      }
    }
  }

  public String getLocation() {
    return location;
  }

  public String getCapability() {
    return capability;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public ResultView getDescribedBy() {
    return describedBy;
  }

}
