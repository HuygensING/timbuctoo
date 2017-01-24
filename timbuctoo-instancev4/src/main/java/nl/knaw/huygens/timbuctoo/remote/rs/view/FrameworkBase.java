package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.ResultIndex;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.ResultIndexPivot;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for viewing a ResultIndex as a framework.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class FrameworkBase {

  private List<ResultView> level3;
  private List<ResultView> level2;
  private List<ResultView> level1;
  private List<ResultView> level0;

  public FrameworkBase(ResultIndex resultIndex) {
    init(resultIndex, new Interpreter());
  }

  public FrameworkBase(ResultIndex resultIndex, Interpreter interpreter) {
    init(resultIndex, interpreter);
  }

  private void init(ResultIndex resultIndex, Interpreter interpreter) {
    ResultIndexPivot pivot = new ResultIndexPivot(resultIndex);

    level3 = pivot.listRsRootResultsByLevel(3).stream()
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());
    level2 = pivot.listRsRootResultsByLevel(2).stream()
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());
    level1 = pivot.listRsRootResultsByLevel(1).stream()
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());
    level0 = pivot.listRsRootResultsByLevel(0).stream()
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ResultView> getLevel3() {
    return level3;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ResultView> getLevel2() {
    return level2;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ResultView> getLevel1() {
    return level1;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ResultView> getLevel0() {
    return level0;
  }
}
