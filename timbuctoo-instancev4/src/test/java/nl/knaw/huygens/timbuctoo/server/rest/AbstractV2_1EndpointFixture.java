package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.concordion.extensions.HttpCommandExtension;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.concordion.extensions.ReplaceEmbeddedStylesheetExtension;
import org.concordion.api.extension.Extension;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;

public abstract class AbstractV2_1EndpointFixture {

  @Extension
  public HttpCommandExtension commandExtension = new HttpCommandExtension(this::doHttpCommand, this::validate, false);
  @Extension
  public ReplaceEmbeddedStylesheetExtension removeExtension = new ReplaceEmbeddedStylesheetExtension(
    "/nl/knaw/huygens/timbuctoo/server/rest/concordion.css"
  );

  /**
   * Implements the actual http call for the concordion HTTPCommand.
   */
  protected Response doHttpCommand(HttpRequest httpRequest) {
    WebTarget target = ClientBuilder.newClient()
      .target(httpRequest.server != null ? httpRequest.server : "http://test.repository.huygens.knaw.nl")
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

  public abstract String validate(HttpExpectation expectation, HttpResult reality);

}
