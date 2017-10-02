package nl.knaw.huygens.timbuctoo.search.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.BeforeClass;
import org.junit.Test;

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
  public void elaborateQueryAsIs() throws Exception {
    String originalQuery = createQuery1();
    JsonNode qAsNode = eSearch.elaborateQuery(originalQuery, null, -1);

    assertThat(qAsNode.toString(), equalTo(originalQuery.replaceAll("\n|\\s", "")));
    JsonNode sizeNode = qAsNode.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(3));
  }

  @Test
  public void elaborateQueryWithSize() throws Exception {
    String originalQuery = createQuery1();
    JsonNode qAsNode = eSearch.elaborateQuery(originalQuery, null, 4);

    JsonNode sizeNode = qAsNode.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(4));
  }

  @Test
  public void elaborateQueryWithoutSize() throws Exception {
    String originalQuery = createQueryWithoutSize();
    JsonNode qAsNode = eSearch.elaborateQuery(originalQuery, null, 4);

    JsonNode sizeNode = qAsNode.findValue("size");
    assertThat(sizeNode.asInt(), equalTo(4));
  }

  private String createQuery1() {
    return "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"},\n" +
      "        {\"account_number\": \"desc\"}\n" +
      "    ]\n" +
      "}";
  }

  private String createQueryWithoutSize() {
    return "{\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"},\n" +
      "        {\"account_number\": \"desc\"}\n" +
      "    ]\n" +
      "}";
  }
}
