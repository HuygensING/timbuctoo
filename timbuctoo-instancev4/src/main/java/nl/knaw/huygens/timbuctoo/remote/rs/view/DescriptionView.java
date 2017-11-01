package nl.knaw.huygens.timbuctoo.remote.rs.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Description;
import nl.knaw.huygens.timbuctoo.remote.rs.discover.Result;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Created on 2017-11-01 10:35.
 */
public class DescriptionView {

  private JsonNode content;
  private String rawContent;
  private ErrorView error;

  public DescriptionView(Result<Description> descriptionResult) {
    init(descriptionResult, new Interpreter());
  }

  public DescriptionView(Result<Description> descriptionResult, Interpreter interpreter) {
    init(descriptionResult, interpreter);
  }

  private void init(Result<Description> descriptionResult, Interpreter interpreter) {
    if (descriptionResult.getContent().isPresent()) {
      Description description = descriptionResult.getContent().get();
      String mimeType = description.getDescribedByLink().getType().orElse(null);
      Optional<RDFFormat> maybeFormat = Rio.getParserFormatForMIMEType(mimeType);
      if (!maybeFormat.isPresent()) {
        String filename = descriptionResult.getUri().toString();
        maybeFormat = Rio.getParserFormatForFileName(filename);
      }
      if (maybeFormat.isPresent()) {
        createDescriptionNode(description, maybeFormat.get(), interpreter);
      } else {
        rawContent = description.getRawContent();
      }
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public JsonNode getContent() {
    return content;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getRawContent() {
    return rawContent;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public ErrorView getError() {
    return error;
  }

  private void createDescriptionNode(Description description, RDFFormat parseFormat, Interpreter interpreter) {
    try {
      Model model = Rio.parse(IOUtils.toInputStream(description.getRawContent()), "", parseFormat);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Rio.write(model, out, RDFFormat.JSONLD);
      ObjectMapper mapper = new ObjectMapper();
      content = mapper.readTree(out.toString());
    } catch (IOException e) {
      rawContent = description.getRawContent();
      error = new ErrorView(e, interpreter);
    }
  }
}
