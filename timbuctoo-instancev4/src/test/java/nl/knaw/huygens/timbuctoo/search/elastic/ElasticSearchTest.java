package nl.knaw.huygens.timbuctoo.search.elastic;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import static org.junit.Assume.assumeTrue;

/**
 * Tests in this class assume an elasticSearch instance at localhost:9200.
 * The instance should have an index with the name 'bank'.
 * See example at
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/_exploring_your_data.html#_loading_the_sample_dataset
 */
public class ElasticSearchTest {

  private static final String hostname = "localhost";
  private static final int port = 9200;
  private static final String username = "elastic";
  private static final String password = "changeme";

  private static ElasticSearch eSearch;

  @BeforeClass
  public static void initialize() throws Exception {
    assumeTrue("No host at " + hostname + ":" + port + ", skipping tests", hostIsAvailable());
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
    String elasticsearchQuery = "{\n" +
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
    Optional<String> token = Optional.empty();
    int preferredPageSize = 3;
    eSearch.query(index, elasticsearchQuery, token, preferredPageSize);
  }

}
