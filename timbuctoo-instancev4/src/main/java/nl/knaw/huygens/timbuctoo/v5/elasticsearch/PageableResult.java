package nl.knaw.huygens.timbuctoo.v5.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Created on 2017-10-04 10:54.
 */
public class PageableResult {

  public static final String ID_FIELD = "uri";

  private final JsonNode queryNode;
  private final JsonNode resultNode;

  public PageableResult(JsonNode queryNode, JsonNode resultNode) {
    this.queryNode = queryNode;
    this.resultNode = resultNode;
  }

  public String getQuery() {
    return queryNode.toString();
  }

  public String getResult() {
    return resultNode.toString();
  }

  public List<String> getIdList() {
    return resultNode.findPath("hits").findPath("hits").findValuesAsText(ID_FIELD);
  }

  public String getToken() {
    String token = null;
    List<JsonNode> sortNodes = resultNode.findValues("sort");
    if (!sortNodes.isEmpty()) {
      token = sortNodes.get(sortNodes.size() - 1).toString();
    }
    return token;
  }

  public int getTotalHits() {
    return resultNode.findPath("hits").findPath("total").asInt();
  }

  public int getSearchTime() {
    return resultNode.findPath("took").asInt();
  }
}
