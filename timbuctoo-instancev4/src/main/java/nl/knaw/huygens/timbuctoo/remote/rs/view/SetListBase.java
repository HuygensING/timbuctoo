package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Result;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.ResultIndex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  Base class for viewing a ResultIndex as a set list.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class SetListBase {

  private List<SetItemView> setDetails;
  private List<ResultView> explorationAttempts;
  private List<ResultView> resultsWithExceptions;
  private Set<String> invalidUris;

  public SetListBase(ResultIndex resultIndex) {
    init(resultIndex, new Interpreter() {});
  }

  public SetListBase(ResultIndex resultIndex, Interpreter interpreter) {
    init(resultIndex, interpreter);
  }

  @SuppressWarnings("unchecked")
  private void init(ResultIndex resultIndex, Interpreter interpreter) {

    setDetails = filterCapabilityLists(resultIndex, interpreter);

    explorationAttempts = resultIndex.getResultMap().values().stream()
      .filter(result -> result.getOrdinal() == 0)
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());

    resultsWithExceptions = resultIndex.getResultMap().values().stream()
      .filter(result -> !result.getErrors().isEmpty())
      .map(result -> new ResultView(result, interpreter))
      .collect(Collectors.toList());

    invalidUris = resultIndex.getInvalidUris();
  }

  static List<SetItemView> filterCapabilityLists(ResultIndex resultIndex, Interpreter interpreter) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      //change  above to result.getContent().ifPresent()
      .map(result -> (Result<Urlset>) result) // save cast because of previous filter
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(Capability.DESCRIPTION.xmlValue))
      .map(urlsetResult -> urlsetResult.getContent().orElse(null))
      .map(Urlset::getItemList)
      .flatMap(Collection::stream)
      .map(rsItem -> new SetItemView(resultIndex, rsItem, interpreter))
      //below is to prevent duplicate locations because of redirection (Eg. with .well-known/resource)
      .collect(Collectors.toMap(SetItemView::getLocation, loc -> loc, (loc1, loc2) -> loc1)).values().stream()
      .collect(Collectors.toList());
  }


  public List<SetItemView> getSetDetails() {
    return setDetails;
  }

  public List<ResultView> getExplorationAttempts() {
    return explorationAttempts;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<ResultView> getResultsWithExceptions() {
    return resultsWithExceptions;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<String> getInvalidUris() {
    return invalidUris;
  }
}
