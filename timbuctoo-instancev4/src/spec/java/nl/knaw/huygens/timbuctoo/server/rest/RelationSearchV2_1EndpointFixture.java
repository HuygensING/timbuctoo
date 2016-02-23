package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.List;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class RelationSearchV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  public String getPersonSearchId() {
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<>("Content-type",  "application/json"));
    headers.add(new AbstractMap.SimpleEntry<>("VRE_ID",  "WomenWriters"));

    HttpRequest postRequest =
        new HttpRequest("POST", "/v2.1/search/wwpersons", headers, "{}", null, Lists.newArrayList());

    Response response = executeRequestUsingJaxRs(postRequest);
    String searchPath = response.getHeaderString("Location").replaceAll("http://[^/]+/", "");
    return searchPath.replaceAll(".*\\/", "");
  }

  public String isFullyQualified(String url) {

    if (StringUtils.isBlank(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
      return "not a fully qualified HTTP url";
    } else {
      return "a fully qualified HTTP url";
    }
  }

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.hasBody()) {
      try {
        JSONCompareResult result = JSONCompare.compareJSON(
            expectation.body,
            reality.getBody(),
            new RegexJsonComparator(JSONCompareMode.LENIENT)
        );

        return result.getMessage();
      } catch (JSONException e) {
        return ExceptionUtils.getStackTrace(e);
      }
    } else {
      return "";
    }
  }

  @Override
  protected WebTarget returnUrlToMockedOrRealServer(String serverAddress) {
    String address = serverAddress != null ? serverAddress : "http://test.repository.huygens.knaw.nl";
    return ClientBuilder.newClient().target(address);
  }

}
