package nl.knaw.huygens.timbuctoo.v5.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROVENANCE_ENTITIES;
public class TimbuctooRdfIdHelper {
  private final String baseUri;

  @JsonCreator
  public TimbuctooRdfIdHelper(@JsonProperty("rdfBaseUri") String baseUri) {
    this.baseUri = baseUri.replaceAll("/$", ""); // remove possible last slash
  }

  private static String encode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      //will never happen
      throw new RuntimeException(e);
    }
  }

  public static String provenanceEntity(String dataSetId, String userId) {
    return TIM_PROVENANCE_ENTITIES + encode(dataSetId) + "/" + encode(userId) + "/" + UUID.randomUUID();
  }

  public String dataSet(String ownerId, String dataSetId) {
    return baseUri + "/datasets/" + encode(ownerId) + "/" + encode(dataSetId);
  }

  public String getBaseUri() {
    return baseUri;
  }
}
