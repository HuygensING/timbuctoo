package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ImmutableContextData;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;

import java.io.IOException;
import java.util.Optional;

public class SetCustomProvenanceMutation implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final DataSetRepository dataSetRepository;
  private final String dataSetName;
  private final String ownerId;

  public SetCustomProvenanceMutation(DataSetRepository dataSetRepository, String dataSetId) {
    this.dataSetRepository = dataSetRepository;
    Tuple<String, String> dataSetIdSplit = DataSetMetaData.splitCombinedId(dataSetId);
    dataSetName = dataSetIdSplit.getRight();
    ownerId = dataSetIdSplit.getLeft();
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    ImmutableContextData contextData = environment.getContext();
    Optional<User> userOpt = contextData.getUser();
    if (!userOpt.isPresent()) {
      throw new RuntimeException("User should be logged in.");
    }
    User user = userOpt.get();
    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(user, ownerId, dataSetName);
    if (!dataSetOpt.isPresent()) {
      throw new RuntimeException("Data set is not available.");
    }

    DataSet dataSet = dataSetOpt.get();
    if (!contextData.getUserPermissionCheck().hasPermission(dataSet.getMetadata(), Permission.SET_CUSTOM_PROV)) {
      throw new RuntimeException("User should have permissions to set the custom provenance.");
    }

    TreeNode customProvenanceNode = OBJECT_MAPPER.valueToTree(environment.getArgument("customProvenance"));
    try {
      CustomProvenance customProvenance =  OBJECT_MAPPER.treeToValue(customProvenanceNode, CustomProvenance.class);
      dataSet.setCustomProvenance(customProvenance);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return ImmutableMap.of("message", "Custom provenance is set.");
  }

  public static class CustomProvenance {
    @JsonProperty("fields")
    private CustomProvenanceValueFieldInput fields;

    @JsonCreator
    public CustomProvenance(@JsonProperty("fields") CustomProvenanceValueFieldInput fields) {
      this.fields = fields;
    }

    private CustomProvenanceValueFieldInput getFields() {
      return fields;
    }
  }

  private static class CustomProvenanceValueFieldInput {
    @JsonProperty
    private String uri;
    @JsonProperty
    private boolean isList;
    @JsonProperty
    private String valueType;
    @JsonProperty
    private CustomProvenance object;

    private CustomProvenanceValueFieldInput(
      @JsonProperty("uri") String uri,
      @JsonProperty("isList") boolean isList,
      @JsonProperty ("valueType") String valueType,
      @JsonProperty("object") CustomProvenance object) {
      this.uri = uri;
      this.isList = isList;
      this.valueType = valueType;
      this.object = object;
    }

    public String getUri() {
      return uri;
    }

    public boolean isList() {
      return isList;
    }

    public String getValueType() {
      return valueType;
    }


    public CustomProvenance getObject() {
      return object;
    }
  }

}
