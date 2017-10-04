package nl.knaw.huygens.timbuctoo.v5.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class ElasticSearch {

  public static final String UNIQUE_FIELD_NAME = "_uid";

  private static final String METHOD_GET = "GET";

  private final RestClient restClient;
  private final ObjectMapper mapper;

  @JsonCreator
  public ElasticSearch(@JsonProperty("hostname") String hostname, @JsonProperty("port") int port,
                       @JsonProperty("username") String username, @JsonProperty("password") String password) {
    Header[] headers = {
      new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
      new BasicHeader("Role", "Read")};
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    restClient = RestClient.builder(new HttpHost(hostname, port))
                           .setDefaultHeaders(headers)
                           .setHttpClientConfigCallback(
                             builder -> builder.setDefaultCredentialsProvider(credentialsProvider))
                           .build();
    mapper = new ObjectMapper();
  }

  public PageableResult query(String index, String elasticSearchQuery, String token, int preferredPageSize)
    throws IOException {
    String endpoint = index.endsWith("_search") ? index : index.endsWith("/") ? index + "_search" : index + "/_search";
    JsonNode queryNode = elaborateQuery(elasticSearchQuery, token, preferredPageSize);
    Map<String, String> params = Collections.singletonMap("pretty", "true");

    HttpEntity entity = new NStringEntity(queryNode.toString(), ContentType.APPLICATION_JSON);
    Response response = restClient.performRequest(METHOD_GET, endpoint, params, entity);

    JsonNode responseNode = mapper.readTree(response.getEntity().getContent());
    return new PageableResult(queryNode, responseNode);
  }

  protected ObjectNode elaborateQuery(String elasticSearchQuery, String token, int preferredPageSize)
    throws IOException {
    // size -1 gives the default 10 results. size 0 gives 0 results. totals are always given.
    // requests without a 'query' clause are legal, so don't check.
    // if 'search_after' is present, 'sort' must contain just as many fields of same type (not checked).
    // 'sort' must be present and must contain "..one unique value per document.." (we check on/put UNIQUE_FIELD_NAME).
    ObjectNode node = (ObjectNode) mapper.readTree(elasticSearchQuery);

    // size
    node.put("size", preferredPageSize);

    // search_after
    if (token != null && !token.isEmpty()) {
      ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
      if (searchAfterNode == null) {
        searchAfterNode = node.putArray("search_after");
      }
      searchAfterNode.removeAll();
      searchAfterNode.addAll((ArrayNode) mapper.readTree(token));
    } else {
      node.remove("search_after");
    }

    // sort
    ArrayNode sortNode = (ArrayNode) node.findValue("sort");
    if (sortNode == null) {
      sortNode = node.putArray("sort");
    }
    if (sortNode.findValue(UNIQUE_FIELD_NAME) == null) {
      ObjectNode objNode = JsonNodeFactory.instance.objectNode();
      objNode.put(UNIQUE_FIELD_NAME, "desc");
      sortNode.add(objNode);
    }
    return node;
  }


}
