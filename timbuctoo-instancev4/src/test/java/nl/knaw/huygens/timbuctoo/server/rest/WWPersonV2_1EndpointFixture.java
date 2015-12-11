package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpCommandExtension;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.concordion.extensions.ReplaceEmbeddedStylesheetExtension;
import org.apache.commons.lang3.StringUtils;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.AbstractMap;

@RunWith(ConcordionRunner.class)
public class WWPersonV2_1EndpointFixture {
  @Extension
  public HttpCommandExtension commandExtension = new HttpCommandExtension(this::doHttpCommand, false);
  @Extension
  public ReplaceEmbeddedStylesheetExtension removeExtension = new ReplaceEmbeddedStylesheetExtension("/nl/knaw/huygens/timbuctoo/server/rest/concordion.css");

//  @Rule
//  public final ResourceTestRule resources = ResourceTestRule.builder().addResource(new WWPersonCollectionV2_1EndPoint()).build();

  private Response doHttpCommand(HttpRequest httpRequest) {
    WebTarget target = ClientBuilder.newClient()
      .target(httpRequest.server != null ? httpRequest.server : "http://acc.repository.huygens.knaw.nl")
      .path(httpRequest.url);

    for (AbstractMap.SimpleEntry<String, String> queryParameter : httpRequest.queryParameters) {
      target = target.queryParam(queryParameter.getKey(), queryParameter.getValue());
    }

    Invocation.Builder request = target
      .request();

    for (AbstractMap.SimpleEntry<String, String> header : httpRequest.headers) {
      request = request.header(header.getKey(), header.getValue());
    }

    if (httpRequest.body != null) {
      return request.method(httpRequest.method, Entity.json(httpRequest.body));
    } else {
      return request.method(httpRequest.method);
    }
  }

  public String isEmpty(String value) {
    return StringUtils.isBlank(value) ? "empty" : "not empty";
  }

  public int getNumberOfItems(HttpResult result) {
    try {
      JsonNode jsonNode = new ObjectMapper().readTree(result.getBody().getBytes());
      return Lists.newArrayList(jsonNode.elements()).size();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean returnFalse() {
    return false;
  }

}
