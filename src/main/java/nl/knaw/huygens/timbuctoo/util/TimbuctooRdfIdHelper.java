package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TimbuctooRdfIdHelper {
  private final String baseUri;

  @JsonCreator
  public TimbuctooRdfIdHelper(@JsonProperty("rdfBaseUri") String baseUri) {
    this.baseUri = baseUri.replaceAll("/$", ""); // remove possible last slash
  }

  private static String encode(String input) {
    return URLEncoder.encode(input, StandardCharsets.UTF_8);
  }

  public String dataSetBaseUri(String ownerId, String dataSetId) {
    return (baseUri + "/datasets/" + encode(ownerId) + "/" + encode(dataSetId) + "/");
  }

  public String instanceBaseUri() {
    return baseUri;
  }
}
