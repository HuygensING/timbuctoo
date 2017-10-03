package nl.knaw.huygens.timbuctoo.search.elastic;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeTrue;

/**
 * Created on 2017-10-02 12:57.
 */
public class ElasticSearchTest {

  private static ElasticSearch eSearch;

  @BeforeClass
  public static void initialize() throws Exception {
    eSearch = new ElasticSearch("", 0, "", null);
  }

  @Test
  public void elaborateEmptyQuery() throws Exception {
    JsonNode node = eSearch.elaborateQuery(createEmptyQuery(), null, 5);
    JsonNode sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(5));
    assertThat(node.has("search_after"), equalTo(false));
    //System.out.println(node.toString()); // {"size":5}

    ArrayNode token = JsonNodeFactory.instance.arrayNode();
    token.add(123);

    node = eSearch.elaborateQuery(createEmptyQuery(), token.toString(), 6);
    sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(6));
    ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
    assertThat(searchAfterNode.elements().next().asInt(), equalTo(123));
    ArrayNode sortNode = (ArrayNode) node.findValue("sort");
    assertThat(sortNode.elements().next().toString(), equalTo("{\"_uid\":\"desc\"}"));
    //System.out.println(node.toString()); // {"size":6,"search_after":[123],"sort":[{"_uid":"desc"}]}
  }

  @Test
  public void elaborateCompleteQuery() throws Exception {
    JsonNode node = eSearch.elaborateQuery(createCompleteQuery(), null, 5);
    JsonNode sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(5));
    assertThat(node.has("search_after"), equalTo(false));
    ArrayNode sortNode = (ArrayNode) node.findValue("sort");
    assertThat(sortNode.toString(), equalTo("[{\"balance\":\"asc\"},{\"_uid\":\"desc\"}]"));
    //System.out.println(node.toString());
    // {"size":5,"query":{"match":{"gender":"F"}},"sort":[{"balance":"asc"},{"_uid":"desc"}]}

    ArrayNode token = JsonNodeFactory.instance.arrayNode();
    token.add(49223);
    token.add(123);

    node = eSearch.elaborateQuery(createCompleteQuery(), token.toString(), 6);
    sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(6));
    ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
    assertThat(searchAfterNode.toString(), equalTo("[49223,123]"));
    sortNode = (ArrayNode) node.findValue("sort");
    assertThat(sortNode.toString(), equalTo("[{\"balance\":\"asc\"},{\"_uid\":\"desc\"}]"));
    //System.out.println(node.toString());
    // {"size":6,"query":{"match":{"gender":"F"}},"search_after":[49223,123],"sort":[{"balance":"asc"},{"_uid":"desc"}]}
  }

  @Test
  public void elaborateIncompleteQuery() throws Exception {
    JsonNode node = eSearch.elaborateQuery(createIncompleteQuery(), null, 5);
    JsonNode sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(5));
    assertThat(node.has("search_after"), equalTo(false));
    ArrayNode sortNode = (ArrayNode) node.findValue("sort");
    assertThat(sortNode.toString(), equalTo("[{\"balance\":\"asc\"}]"));
    //System.out.println(node.toString());
    // {"size":5,"query":{"match":{"gender":"F"}},"sort":[{"balance":"asc"}]}

    ArrayNode token = JsonNodeFactory.instance.arrayNode();
    token.add(49223);
    token.add(123);

    node = eSearch.elaborateQuery(createCompleteQuery(), token.toString(), 6);
    sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(6));
    ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
    assertThat(searchAfterNode.toString(), equalTo("[49223,123]"));
    sortNode = (ArrayNode) node.findValue("sort");
    assertThat(sortNode.toString(), equalTo("[{\"balance\":\"asc\"},{\"_uid\":\"desc\"}]"));
    //System.out.println(node.toString());
    //{"size":6,"query":{"match":{"gender":"F"}},"search_after":[49223,123],"sort":[{"balance":"asc"},{"_uid":"desc"}]}
  }

  private String createEmptyQuery() {
    return "{}";
  }

  private String createCompleteQuery() {
    return "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"search_after\": [1314, 315],\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"},\n" +
      "        {\"_uid\": \"desc\"}\n" +
      "    ]\n" +
      "}";
  }

  private String createIncompleteQuery() {
    return "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"}\n" +
      "    ]\n" +
      "}";
  }

}

