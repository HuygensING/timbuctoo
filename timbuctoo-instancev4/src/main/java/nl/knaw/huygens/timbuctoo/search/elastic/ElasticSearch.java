package nl.knaw.huygens.timbuctoo.search.elastic;

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
import java.util.List;
import java.util.Map;

/**
 * Created on 2017-09-25 15:28.
 */
public class ElasticSearch {

  public static final String FIELD_NAME = "_uid";
  private static final String METHOD_GET = "GET";

  private final RestClient restClient;
  private final ObjectMapper mapper;

  public ElasticSearch(String hostname, int port, String username, String password) {
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

  public PageableResult2 query(String index, String elasticSearchQuery, String token, int preferredPageSize)
    throws IOException {
    String endpoint = index.endsWith("_search") ? index : index.endsWith("/") ? index + "_search" : index + "/_search";
    Map<String, String> params = Collections.singletonMap("pretty", "true");
    HttpEntity entity = new NStringEntity(elasticSearchQuery, ContentType.APPLICATION_JSON);
    Response response = restClient.performRequest(METHOD_GET, endpoint, params, entity);

    // System.out.println(EntityUtils.toString(response.getEntity()));
    JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
    System.err.println("++" + "\n" + jsonNode.toString());
    List<JsonNode> sortNodes = jsonNode.findValues("sort");
    for (JsonNode node : sortNodes) {
      System.out.println(node.toString());
    }
    // for (JsonNode node : sortNodes) {
    //   System.out.println(node.getNodeType().name());
    // }
    return new PageableResult2().setIdFields(jsonNode.findValuesAsText(FIELD_NAME));
  }

  ObjectNode elaborateQuery(String elasticSearchQuery, String token, int preferredPageSize) throws IOException {
    // size -1 gives the default 10 results. size 0 gives 0 results. totals are always given.
    // requests without a 'query' clause are legal, so don't check.
    // if 'search_after' is present, 'sort' must contain just as many fields of same type (not checked).
    // if 'search_after' is present, 'sort' must contain "..one unique value per document.."
    // (we check on/put FIELD_NAME).
    ObjectNode node = (ObjectNode) mapper.readTree(elasticSearchQuery);
    node.put("size", preferredPageSize);
    if (token != null) {
      if (!node.has("search_after")) {
        node.putArray("search_after");
      }
      ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
      searchAfterNode.removeAll();
      searchAfterNode.addAll((ArrayNode) mapper.readTree(token));

      if (!node.has("sort")) {
        node.putArray("sort");
      }
      ArrayNode sortNode = (ArrayNode) node.findValue("sort");
      if (sortNode.findValue(FIELD_NAME) == null) {
        ObjectNode objNode = JsonNodeFactory.instance.objectNode();
        objNode.put(FIELD_NAME, "desc");
        sortNode.add(objNode);
      }

    } else {
      node.remove("search_after");
    }
    return node;
  }


}
