package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Result;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class TreeResultView extends ResultView {

  private List<TreeResultView> children;

  public TreeResultView(Result<?> result) {
    this(result, new Interpreter());
  }

  public TreeResultView(Result<?> result, Interpreter interpreter) {
    super(result, interpreter);
    init(result, interpreter);
  }

  private void init(Result<?> result, Interpreter interpreter) {
    children = result.getChildren().values().stream()
      .map(childResult -> new TreeResultView(childResult, interpreter))
      .collect(Collectors.toList());
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<TreeResultView> getChildren() {
    return children;
  }
}
