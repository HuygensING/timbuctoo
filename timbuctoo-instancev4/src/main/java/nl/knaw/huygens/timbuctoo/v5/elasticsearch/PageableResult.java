package nl.knaw.huygens.timbuctoo.v5.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Created on 2017-10-04 10:54.
 */
public class PageableResult {

  public final String idField;

  private final JsonNode queryNode;
  private final JsonNode resultNode;

  public PageableResult(JsonNode queryNode, JsonNode resultNode) {
    this(queryNode, resultNode, "uri");
  }

  public PageableResult(JsonNode queryNode, JsonNode resultNode, String idField) {
    this.queryNode = queryNode;
    this.resultNode = resultNode;
    this.idField = idField;
  }


  public String getQuery() {
    return queryNode.toString();
  }

  public String getResult() {
    return resultNode.toString();
  }

  public List<String> getIdList() {
    return resultNode.findPath("hits").findPath("hits").findValuesAsText(idField);
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
