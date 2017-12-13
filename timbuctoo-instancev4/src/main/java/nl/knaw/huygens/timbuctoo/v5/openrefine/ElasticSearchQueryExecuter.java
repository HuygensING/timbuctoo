package nl.knaw.huygens.timbuctoo.v5.openrefine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Created by meindertk on 31-10-2017.
 */
public class ElasticSearchQueryExecuter implements ReconciliationQueryExecutor {

  public static final String UNIQUE_FIELD_NAME = "_uid";
  private static final String METHOD_GET = "GET";
  private static RestClient restclient;
  ObjectMapper mapper;

  @Override
  public Map<String, QueryResults> execute(Map<String, Query> query) throws IOException {
    String hostname = "localhost";
    int port = 9200;
    if (restclient == null) {
      getRestclient();
    }
    Optional<String> username = Optional.of("elastic");
    Optional<String> password = Optional.of("changeme");

    Map<String, QueryResults> queryResult = new TreeMap<>();
    for (Map.Entry<String, Query> stringQueryEntry : query.entrySet()) {
      String jsonString = "{" +
        "\"query\": { \"match\": { \"naam.achternaam\":  \"" + stringQueryEntry.getValue().query + "\" } }" +
        "}";
      HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
      Map<String, String> params = Collections.emptyMap();
      Response result = restclient.performRequest("GET", "/scientist/_search", params, entity);
      // System.err.println("-------------");
      // System.err.println("query: " + stringQueryEntry.getValue().query);
      // System.err.println(result.toString());
      // System.err.println("-------------");
      QueryResult qr = new QueryResult();
      qr.id = stringQueryEntry.getKey().substring(1);
      qr.type = new String[]{"String"};
      String content = IOUtils.toString(result.getEntity().getContent(), "UTF-8");
      // System.err.println("content: " + content);
      JSONObject scoreAndName = getScoreAndName(content);
      try {
        if (scoreAndName != null) {
          qr.score = (double) scoreAndName.get("score");
          qr.name = (String) scoreAndName.get("naam");
          qr.match = true;
        } else {
          qr.score = 0.0;
          qr.name = "";
          qr.match = false;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      ArrayList<QueryResult> qrAl = new ArrayList<>();
      qrAl.add(qr);
      QueryResults queryResults = new QueryResults();
      queryResults.result = qrAl;
      queryResult.put(stringQueryEntry.getKey(), queryResults);
    }
    return queryResult;
  }

  private void getRestclient() throws IOException {
    System.err.println("getRestClient");
    String url = "http://127.0.0.1:9200";
    URLConnection connection = new URL(url).openConnection();
    HttpURLConnection httpConnection = (HttpURLConnection) connection;

    int status = httpConnection.getResponseCode();
    // System.err.println("-------------");
    // System.err.println(" url: " + url);
    // System.err.println(" status: " + status);
    // System.err.println("-------------");
    restclient = RestClient.builder(new HttpHost("localhost", 9200))
                           .setMaxRetryTimeoutMillis(60000).build();
  }

  private JSONObject getScoreAndName(String stringResult) {
    JSONObject resObject = new JSONObject();
    try {
      JSONObject jsonObj = new JSONObject(stringResult);
      JSONObject hits = jsonObj.getJSONObject("hits");
      JSONArray hitsInHits = hits.getJSONArray("hits");
      for (int i = 0; i < hitsInHits.length(); i++) {
        JSONObject hit = (JSONObject) hitsInHits.get(i);
        resObject.put("score", hit.get("_score"));
        String achternaam = ((JSONObject) ((JSONObject) hit
          .get("_source")).get("naam")).get("achternaam").toString();
        resObject.put("naam", achternaam);
      }
    } catch (JSONException e) {
      if (stringResult.isEmpty()) {
        System.err.println("er is iets mis met: [empty input]");
      } else {
        System.err.println("er is iets mis met: " + stringResult);
      }
      // e.printStackTrace();
    }
    return resObject;
  }
}
