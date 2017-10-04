package nl.knaw.huygens.timbuctoo.v5.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    // empty query with and without token.
    String query = "{}";
    String expectedSort = "{\"" + ElasticSearch.UNIQUE_FIELD_NAME + "\":\"desc\"}";

    JsonNode node = eSearch.elaborateQuery(query, null, 5);

    JsonNode sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(5));
    assertThat(node.has("search_after"), equalTo(false));
    JsonNode sortNode = node.findValue("sort");
    assertThat(sortNode.elements().next().toString(), equalTo(expectedSort));
    String expected = "{\"size\":5,\"sort\":[" + expectedSort + "]}";
    assertThat(node.toString(), equalTo(expected));
    //System.out.println(node.toString()); // {"size":5,"sort":[{"_uid":"desc"}]}

    ArrayNode token = JsonNodeFactory.instance.arrayNode();
    token.add(123);
    expected = "{\"size\":6,\"search_after\":[123],\"sort\":[" + expectedSort + "]}";
    node = eSearch.elaborateQuery(query, token.toString(), 6);

    sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(6));
    ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
    assertThat(searchAfterNode.elements().next().asInt(), equalTo(123));
    sortNode = node.findValue("sort");
    assertThat(sortNode.elements().next().toString(), equalTo(expectedSort));
    assertThat(node.toString(), equalTo(expected));
    //System.out.println(node.toString()); // {"size":6,"search_after":[123],"sort":[{"_uid":"desc"}]}
  }

  @Test
  public void elaborateCompleteQuery() throws Exception {
    String query = "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"search_after\": [1314, 315],\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"},\n" +
      "        {\"" + ElasticSearch.UNIQUE_FIELD_NAME + "\": \"desc\"}\n" +
      "    ]\n" +
      "}";
    testElaborate(query);
  }

  @Test
  public void elaborateIncompleteQuery() throws Exception {
    // missing unique field.
    String query = "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"search_after\": [1314, 315],\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"}\n" +
      "    ]\n" +
      "}";
    testElaborate(query);
  }

  private void testElaborate(String query) throws Exception {
    // test elaborate with and without token.

    JsonNode node = eSearch.elaborateQuery(query, null, 5);

    JsonNode sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(5));
    assertThat(node.has("search_after"), equalTo(false));
    JsonNode sortNode = node.findValue("sort");
    Iterator<JsonNode> sortIter = sortNode.elements();
    String expectedSort1 = "{\"balance\":\"asc\"}";
    String expectedSort2 = "{\"" + ElasticSearch.UNIQUE_FIELD_NAME + "\":\"desc\"}";
    assertThat(sortIter.next().toString(), equalTo(expectedSort1));
    assertThat(sortIter.next().toString(), equalTo(expectedSort2));
    String expectedSort = expectedSort1 + "," + expectedSort2;
    String expected = "{\"size\":5,\"query\":{\"match\":{\"gender\":\"F\"}},\"sort\":[" + expectedSort + "]}";
    assertThat(node.toString(), equalTo(expected));
    // System.out.println(node.toString());
    // {"size":5,"query":{"match":{"gender":"F"}},"sort":[{"balance":"asc"},{"_uid":"desc"}]}

    ArrayNode token = JsonNodeFactory.instance.arrayNode();
    token.add(49223);
    token.add(123);
    expected = "{\"size\":6,\"query\":{\"match\":{\"gender\":\"F\"}},\"search_after\":[49223,123]," +
      "\"sort\":[" + expectedSort + "]}";
    node = eSearch.elaborateQuery(query, token.toString(), 6);

    sizeNode = node.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(6));
    ArrayNode searchAfterNode = (ArrayNode) node.findValue("search_after");
    assertThat(searchAfterNode.toString(), equalTo("[49223,123]"));
    sortNode = node.findValue("sort");
    sortIter = sortNode.elements();
    assertThat(sortIter.next().toString(), equalTo(expectedSort1));
    assertThat(sortIter.next().toString(), equalTo(expectedSort2));
    assertThat(node.toString(), equalTo(expected));
    // System.out.println(node.toString());
    // {"size":6,"query":{"match":{"gender":"F"}},"search_after":[49223,123],"sort":[{"balance":"asc"},{"_uid":"desc"}]}
  }

}

