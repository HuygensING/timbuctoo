package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import org.immutables.value.Value;

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData.createCombinedId;


public class BasicDataSetMetaData implements DataSetMetaData {

  private final String dataSetId;
  private final String ownerId;
  private final String baseUri;
  private final String uriPrefix;
  private final String combinedId;
  private boolean isPromoted;
  private boolean isPublic;


  @JsonCreator
  public BasicDataSetMetaData(@JsonProperty("ownerId") String ownerId,
                              @JsonProperty("dataSetId") String dataSetId,
                              @JsonProperty("baseUri") String baseUri,
                              @JsonProperty("uriPrefix") String uriPrefix,
                              @JsonProperty("promoted") boolean promoted,
                              @JsonProperty("public") boolean isPublic) throws IllegalDataSetNameException {
    if (!ownerId.matches(VALID_ID) || !dataSetId.matches(VALID_ID)) {
      throw new IllegalDataSetNameException("Owner id and dataSet id should match " + VALID_ID);
    }

    this.dataSetId = dataSetId;
    this.ownerId = ownerId;
    this.combinedId = createCombinedId(ownerId, dataSetId);
    this.baseUri = baseUri;
    this.uriPrefix = uriPrefix;
    this.isPromoted = promoted;
    this.isPublic = isPublic;
  }

  @Override
  public String getDataSetId() {
    return dataSetId;
  }

  @Override
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * Returns the baseUri that is used to resolve relative uri's in uploaded rdf files that have no explicit baseUri set.
   */
  @Override
  public String getBaseUri() {
    return baseUri;
  }

  /**
   * Returns a uri that you can use to generate dataSet-local uri's
   */
  @Override
  public String getUriPrefix() {
    return uriPrefix;
  }

  @Override
  public String getCombinedId() {
    return combinedId;
  }

  @Override
  @Value.Auxiliary
  public boolean isPromoted() {
    return isPromoted;
  }

  @Override
  @Value.Auxiliary
  public boolean isPublic() {
    return isPublic;
  }
}
