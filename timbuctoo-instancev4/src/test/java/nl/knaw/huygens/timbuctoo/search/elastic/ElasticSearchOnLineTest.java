package nl.knaw.huygens.timbuctoo.search.elastic;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeTrue;

/**
 * Tests in this class assume an elasticSearch instance at localhost:9200.
 * The instance should have an index with the name 'bank'.
 * See example at
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/_exploring_your_data.html#_loading_the_sample_dataset
 */
public class ElasticSearchOnLineTest {

  private static final String hostname = "localhost";
  private static final int port = 9200;
  private static final String username = "elastic";
  private static final String password = "changeme";

  private static ElasticSearch eSearch;

  @BeforeClass
  public static void initialize() throws Exception {
    assumeTrue("No host at " + hostname + ":" + port + ", skipping tests." +
      "\nPlease start an ElasticSearch instance at the specified host and port.", hostIsAvailable());
    eSearch = new ElasticSearch(hostname, port, username, password);
  }

  private static boolean hostIsAvailable() {
    try (Socket s = new Socket(hostname, port)) {
      return true;
    } catch (IOException ex) {
        /* ignore */
    }
    return false;
  }

  @Test
  public void querySimple() throws Exception {
    String index = "bank";
    String elasticsearchQuery = createQuery1();
    String token = null;
    int preferredPageSize = 3;
    PageableResult2 result = eSearch.query(index, elasticsearchQuery, token, preferredPageSize);

    //assertThat(result.getIdFields().size(), equalTo(3));
    //assertThat(result.getIdFields(), contains("820", "315", "490"));

  }

  @Test(expected = org.elasticsearch.client.ResponseException.class)
  public void queryAndIndexDoesNotExist() throws Exception {
    String index = "does_not_exist";
    String elasticsearchQuery = createQuery1();
    String token = null;
    int preferredPageSize = 3;
    eSearch.query(index, elasticsearchQuery, token, preferredPageSize);
  }

  @Test
  public void queryWithFieldName() throws Exception {
    String index = "bank";
    String elasticsearchQuery = createQuery2();
    String token = null;
    int preferredPageSize = 3;
    PageableResult2 result = eSearch.query(index, elasticsearchQuery, token, preferredPageSize);
    System.out.println(result.getIdFields());
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

  private String createQuery2() {
    return "{\n" +
      "    \"size\": 3,\n" +
      "    \"query\": {\n" +
      "        \"match\" : {\n" +
      "            \"gender\" : \"F\"\n" +
      "        }\n" +
      "    },\n" +
      "    \"sort\": [\n" +
      "        {\"balance\": \"asc\"},\n" +
      "        {\"" + ElasticSearch.FIELD_NAME + "\": \"desc\"}\n" +
      "    ]\n" +
      "}";
  }
}
