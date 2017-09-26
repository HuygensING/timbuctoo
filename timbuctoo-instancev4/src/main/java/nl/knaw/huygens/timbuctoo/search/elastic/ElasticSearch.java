package nl.knaw.huygens.timbuctoo.search.elastic;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created on 2017-09-25 15:28.
 */
public class ElasticSearch {

  public static final String METHOD_GET = "GET";

  private final RestClient restClient;

  public ElasticSearch(String hostname, int port, String username, String password) {
    Header[] headers = {
      new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
      new BasicHeader("Role", "Read")};
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    restClient = RestClient.builder(new HttpHost(hostname, port))
                           .setDefaultHeaders(headers)
                           .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                             public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder builder) {

                               return builder.setDefaultCredentialsProvider(credentialsProvider);
                             }
                           })
                           .build();
  }

  public void query(String index, String elasticsearchQuery, Optional<String> token, int preferredPageSize)
    throws IOException {
    String endpoint = index.endsWith("_search") ? index : index.endsWith("/") ? index + "_search" : index + "/_search";
    Map<String, String> params = Collections.singletonMap("pretty", "true");
    HttpEntity entity = new NStringEntity(elasticsearchQuery, ContentType.APPLICATION_JSON);
    Response response = restClient.performRequest(METHOD_GET, endpoint, params, entity);
    System.out.println(EntityUtils.toString(response.getEntity()));
  }
}
