package nl.knaw.huygens.timbuctoo.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.IllegalDataSetNameException;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData.createCombinedId;
import static nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData.isValidId;

@JsonIgnoreProperties("graph")
public class BasicDataSetMetaData implements DataSetMetaData {
  private final String dataSetId;
  private final String ownerId;
  private final String baseUri;
  private final String uriPrefix;
  private final String combinedId;
  private final boolean promoted;
  private boolean published;
  private final List<ImportInfo> importInfo;

  @JsonCreator
  public BasicDataSetMetaData(@JsonProperty("ownerId") String ownerId,
                              @JsonProperty("dataSetId") String dataSetId,
                              @JsonProperty("baseUri") String baseUri,
                              @JsonProperty("uriPrefix") String uriPrefix,
                              @JsonProperty("promoted") boolean promoted,
                              @JsonProperty("published") boolean published,
                              @JsonProperty("importInfo") List<ImportInfo> importInfo)
      throws IllegalDataSetNameException {
    if (!isValidId(ownerId) || !isValidId(dataSetId)) {
      throw new IllegalDataSetNameException("Owner id and dataSet id should " + VALID_ID_DESCRIPTION);
    }

    this.dataSetId = dataSetId;
    this.ownerId = ownerId;
    this.combinedId = createCombinedId(ownerId, dataSetId);
    this.baseUri = baseUri;
    this.uriPrefix = uriPrefix;
    this.promoted = promoted;
    this.published = published;
    this.importInfo = importInfo;
  }

  public BasicDataSetMetaData(@JsonProperty("ownerId") String ownerId,
                              @JsonProperty("dataSetId") String dataSetId,
                              @JsonProperty("baseUri") String baseUri,
                              @JsonProperty("uriPrefix") String uriPrefix,
                              @JsonProperty("promoted") boolean promoted,
                              @JsonProperty("published") boolean published) throws IllegalDataSetNameException {
    if (!isValidId(ownerId) || !isValidId(dataSetId)) {
      throw new IllegalDataSetNameException("Owner id and dataSet id should " + VALID_ID_DESCRIPTION);
    }

    this.dataSetId = dataSetId;
    this.ownerId = ownerId;
    this.combinedId = createCombinedId(ownerId, dataSetId);
    this.baseUri = baseUri;
    this.uriPrefix = uriPrefix;
    this.promoted = promoted;
    this.published = published;
    this.importInfo = new ArrayList<>(); //For dataSets uploaded directly to Timbuctoo there is no importInfo
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
    return promoted;
  }

  @Override
  @Value.Auxiliary
  public boolean isPublished() {
    return published;
  }

  @Override
  public List<ImportInfo> getImportInfo() {
    return importInfo;
  }

  @Override
  public void publish() {
    this.published = true;
  }
}
