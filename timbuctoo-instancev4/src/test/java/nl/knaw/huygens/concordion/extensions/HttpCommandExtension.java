package nl.knaw.huygens.concordion.extensions;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.internal.listener.AssertResultRenderer;

public class HttpCommandExtension implements ConcordionExtension {
  private final boolean embedCss;
  private HttpCommand httpCommand;

  public HttpCommandExtension(HttpCaller caller) {
    httpCommand = new HttpCommand(caller, "http", "http://huygens.knaw.nl/concordion-http-verifier");
    httpCommand.addListener(new AssertResultRenderer());
    embedCss = true;
  }
  public HttpCommandExtension(HttpCaller caller, boolean embedCss) {
    httpCommand = new HttpCommand(caller, "http", "http://huygens.knaw.nl/concordion-http-verifier");
    httpCommand.addListener(new AssertResultRenderer());
    this.embedCss = embedCss;
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "http", httpCommand);
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "request", new RequestCommand());
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "response", new ResponseCommand());
    if (embedCss) {
      concordionExtender.withEmbeddedCSS(".defaultValue {\n" +
        "  color: gray;\n" +
        "}\n" +
        ".requestCaption, .responseCaption {\n" +
        "  margin-top: 1em;\n" +
        "  font-weight: bold;\n" +
        "  font-size: 0.8em;\n" +
        "}\n" +
        ".requestContent, .responseContent {\n" +
        "  background: #f6f9fc;\n" +
        "  padding: 10px;\n" +
        "  border: 1px solid #d0d4d9;\n" +
        "  font-family: monospace;\n" +
        "  border-radius: 3px;\n" +
        "}");
    }
  }
}
