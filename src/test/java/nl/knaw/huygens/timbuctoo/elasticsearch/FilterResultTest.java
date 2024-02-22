package nl.knaw.huygens.timbuctoo.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-10-04 11:13.
 */
public class FilterResultTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void readResult() throws Exception {
    EsFilterResult pageableResult = new EsFilterResult(createQuery(), createResult(), "_id");

    String expectedQuery = "{\"size\":3,\"query\":{\"match\":{\"gender\":\"F\"}},\"search_after\":[1464," +
      "\"account#174\"],\"sort\":[{\"balance\":\"asc\"},{\"_uid\":\"desc\"}]}";
    assertThat(pageableResult.getQuery(), equalTo(expectedQuery));

    String expectedResultStart = "{\"took\":25,\"timed_out\":false";
    assertThat(pageableResult.getResult().startsWith(expectedResultStart), equalTo(true));

    assertThat(pageableResult.getUriList().size(), equalTo(3));
    // System.out.println(pageableResult.getIdList());
    assertThat(pageableResult.getUriList(), contains("348", "490", "427"));

    String expectedToken = "3";
    assertThat(pageableResult.getNextToken(), equalTo(expectedToken));

    assertThat(pageableResult.getTotal(), equalTo(1000));
    assertThat(pageableResult.getSearchTime(), equalTo(25));
  }

  @Test
  public void readEmptyResult() throws Exception {
    EsFilterResult pageableResult = new EsFilterResult(createQuery(), createEmptyResult(), "_id");

    assertThat(pageableResult.getUriList().size(), equalTo(0));
    assertThat(pageableResult.getNextToken(), equalTo(null));
    assertThat(pageableResult.getTotal(), equalTo(0));
    assertThat(pageableResult.getSearchTime(), equalTo(5));
  }

  private JsonNode createQuery() throws IOException {
    String queryString = """
        {
            "size": 3,
            "query": {
                "match" : {
                    "gender" : "F"
                }
            },
            "search_after": [1464, "account#174"],
            "sort": [
                {"balance": "asc"},
                {"_uid": "desc"}
            ]
        }""";
    return mapper.readTree(queryString);
  }

  private JsonNode createResult() throws IOException {
    String resultString = """
        {
          "took" : 25,
          "timed_out" : false,
          "_shards" : {
            "total" : 5,
            "successful" : 5,
            "skipped" : 0,
            "failed" : 0
          },
          "hits" : {
            "total" : 1000,
            "max_score" : null,
            "hits" : [
              {
                "_index" : "bank",
                "_type" : "account",
                "_id" : "348",
                "_score" : null,
                "_source" : {
                  "account_number" : 348,
                  "balance" : 1360,
                  "firstname" : "Karina",
                  "lastname" : "Russell",
                  "age" : 37,
                  "gender" : "M",
                  "address" : "797 Moffat Street",
                  "employer" : "Limozen",
                  "email" : "karinarussell@limozen.com",
                  "city" : "Riegelwood",
                  "state" : "RI"
                },
                "sort" : [
                  1360,
                  "account#348"
                ]
              },
              {
                "_index" : "bank",
                "_type" : "account",
                "_id" : "490",
                "_score" : null,
                "_source" : {
                  "account_number" : 490,
                  "balance" : 1447,
                  "firstname" : "Strong",
                  "lastname" : "Hendrix",
                  "age" : 26,
                  "gender" : "F",
                  "address" : "134 Beach Place",
                  "employer" : "Duoflex",
                  "email" : "stronghendrix@duoflex.com",
                  "city" : "Allentown",
                  "state" : "ND"
                },
                "sort" : [
                  1447,
                  "account#490"
                ]
              },
              {
                "_index" : "bank",
                "_type" : "account",
                "_id" : "427",
                "_score" : null,
                "_source" : {
                  "account_number" : 427,
                  "balance" : 1463,
                  "firstname" : "Rebekah",
                  "lastname" : "Garrison",
                  "age" : 36,
                  "gender" : "F",
                  "address" : "837 Hampton Avenue",
                  "employer" : "Niquent",
                  "email" : "rebekahgarrison@niquent.com",
                  "city" : "Zarephath",
                  "state" : "NY"
                },
                "sort" : [
                  1463,
                  "account#427"
                ]
              }
            ]
          }
        }
        """;
    return mapper.readTree(resultString);
  }

  private JsonNode createEmptyResult() throws IOException {
    String resultString = """
        {
          "took" : 5,
          "timed_out" : false,
          "_shards" : {
            "total" : 5,
            "successful" : 5,
            "skipped" : 0,
            "failed" : 0
          },
          "hits" : {
            "total" : 0,
            "max_score" : null,
            "hits" : [ ]
          }
        }""";
    return mapper.readTree(resultString);
  }
}
