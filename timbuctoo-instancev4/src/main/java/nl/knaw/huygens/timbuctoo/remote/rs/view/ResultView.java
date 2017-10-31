package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.LinkList;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Result;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class ResultView {

  private String uri;
  private int ordinal;
  private int statusCode;
  private String contentType;
  private int childCount;
  private List<ErrorView> errorList;
  private Set<String> invalidUris;

  // For results with RsRoot as content
  private String capability;

  public ResultView(Result<?> result) {
    init(result, new Interpreter());
  }

  public ResultView(Result<?> result, Interpreter interpreter) {
    init(result, interpreter);
  }

  private void init(Result<?> result, Interpreter interpreter) {
    uri = result.getUri().toString();
    ordinal = result.getOrdinal();
    statusCode = result.getStatusCode();

    contentType = result.getContent()
      .map(o -> o.getClass().getSimpleName())
      .orElse("(no content)");

    Object content = result.getContent().orElse(null);
    if (content != null) {
      if (content instanceof LinkList) {
        LinkList linkList = (LinkList) content;
        childCount = linkList.getValidUris().size();
      } else if (content instanceof RsRoot) {
        RsRoot<?, ?> rsRoot = (RsRoot) content;
        childCount = rsRoot.getItemList().size();
        Optional<Capability> optionalCapa = rsRoot.getCapability();
        if (optionalCapa.isPresent()) {
          capability = optionalCapa.get().xmlValue;
        }
      }
    }

    errorList = result.getErrors().stream()
      .map(throwable -> new ErrorView(throwable, interpreter))
      .collect(Collectors.toList());

    invalidUris = result.getInvalidUris();


  }

  public String getUri() {
    return uri;
  }

  public int getOrdinal() {
    return ordinal;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getContentType() {
    return contentType;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getCapability() {
    return capability;
  }

  public int getChildCount() {
    return childCount;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ErrorView> getErrorList() {
    return errorList;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<String> getInvalidUris() {
    return invalidUris;
  }


}
