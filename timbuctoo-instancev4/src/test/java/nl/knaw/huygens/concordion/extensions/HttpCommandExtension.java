package nl.knaw.huygens.concordion.extensions;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.internal.listener.AssertResultRenderer;

public class HttpCommandExtension implements ConcordionExtension {
  private HttpCommand httpCommand;

  public HttpCommandExtension(HttpCaller caller) {
    httpCommand = new HttpCommand(caller, "http", "http://huygens.knaw.nl/concordion-http-verifier");
    httpCommand.addListener(new AssertResultRenderer());
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "http", httpCommand);
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "request", new RequestCommand());
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "response", new ResponseCommand());
  }

}
