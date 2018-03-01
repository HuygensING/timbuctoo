package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitType;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadAndMergeWithExistingSchema {

  public ReadAndMergeWithExistingSchema() {

  }

  public Map<String, Type> readAndMergeExistingSchemas(DataSet dataSet, Map<String, Type> types) {
    ReadExistingCustomSchema readExistingCustomSchema = new ReadExistingCustomSchema();

    Map<String, List<ExplicitField>> customSchema = readExistingCustomSchema.readExistingSchema(dataSet);

    final Map<String, Type> customTypes = new HashMap<>();

    for (Map.Entry<String, List<ExplicitField>> entry : customSchema.entrySet()) {
      ExplicitType explicitType = new ExplicitType(entry.getKey(), entry.getValue());
      customTypes.put(entry.getKey(), explicitType.convertToType());
    }

    Map<String, Type> mergedTypes;

    MergeSchemas mergeSchemas = new MergeSchemas();
    mergedTypes = mergeSchemas.mergeSchema(types, customTypes);

    return mergedTypes;
  }
}
