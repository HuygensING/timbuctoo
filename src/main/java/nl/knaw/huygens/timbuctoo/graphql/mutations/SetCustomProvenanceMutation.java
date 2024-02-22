package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.CustomProvenance;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;

import java.io.IOException;
import java.util.Optional;

public class SetCustomProvenanceMutation extends Mutation {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final DataSetRepository dataSetRepository;
  private final String dataSetName;
  private final String ownerId;

  public SetCustomProvenanceMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository,
                                     String dataSetId) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
    Tuple<String, String> dataSetIdSplit = DataSetMetaData.splitCombinedId(dataSetId);
    dataSetName = dataSetIdSplit.right();
    ownerId = dataSetIdSplit.left();
  }

  @Override
  public Object executeAction(DataFetchingEnvironment environment) {
    Optional<User> userOpt = environment.getGraphQlContext().get("user");
    if (userOpt.isEmpty()) {
      throw new RuntimeException("User should be logged in.");
    }
    User user = userOpt.get();
    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(user, ownerId, dataSetName);
    if (dataSetOpt.isEmpty()) {
      throw new RuntimeException("Data set is not available.");
    }

    DataSet dataSet = dataSetOpt.get();
    UserPermissionCheck userPermissionCheck = environment.getGraphQlContext().get("userPermissionCheck");
    if (!userPermissionCheck.hasPermission(dataSet.getMetadata(), Permission.SET_CUSTOM_PROV)) {
      throw new RuntimeException("User should have permissions to set the custom provenance.");
    }

    TreeNode customProvenanceNode = OBJECT_MAPPER.valueToTree(environment.getArgument("customProvenance"));
    try {
      CustomProvenance customProvenance = OBJECT_MAPPER.treeToValue(customProvenanceNode, CustomProvenance.class);
      validateCustomProvenance(customProvenance);
      dataSet.setCustomProvenance(customProvenance);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return ImmutableMap.of("message", "Custom provenance is set.");
  }

  private void validateCustomProvenance(CustomProvenance customProvenance) {
    for (CustomProvenance.CustomProvenanceValueFieldInput field : customProvenance.getFields()) {
      if (((field.getValueType() == null) && (field.getObject() == null)) ||
        ((field.getValueType() != null) && (field.getObject() != null))) {
        throw new RuntimeException("Specify either a value type or an object.");
      }

      if (field.getObject() != null) {
        validateCustomProvenance(field.getObject());
      }
    }
  }
}
