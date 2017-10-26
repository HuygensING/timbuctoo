package nl.knaw.huygens.timbuctoo.v5.openrefine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QueryResults {
  @JsonProperty("result")
  public List<QueryResult> queryResults;
}
