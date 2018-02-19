package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;

import java.util.List;
import java.util.Map;

public class MergeExplicitSchemas {
  private final MergeExplicitFields mergeExplicitFields;

  public MergeExplicitSchemas() {
    mergeExplicitFields = new MergeExplicitFields();
  }

  public Map<String, List<ExplicitField>> mergeExplicitSchemas(Map<String, List<ExplicitField>> explicitSchema1,
                                                               Map<String, List<ExplicitField>> explicitSchema2) {
    return explicitSchema1;
  }

}
