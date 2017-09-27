package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaEntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaUpdateException;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaUpdater;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JsonSchemaStore implements SchemaStore {

  private final JsonFileBackedData<Map<String, Type>> schemaFile;


  public JsonSchemaStore(DataProvider dataProvider, File schemaLocation)
    throws IOException {
    schemaFile = JsonFileBackedData.getOrCreate(
      schemaLocation,
      () -> null,
      new TypeReference<Map<String, Type>>() {}
    );

    SchemaUpdater schemaUpdater = newSchema -> {
      try {
        schemaFile.updateData(oldSchema -> newSchema);
      } catch (IOException e) {
        throw new SchemaUpdateException(e);
      }
    };
    dataProvider.subscribeToEntities(new SchemaEntityProcessor(schemaUpdater), null);
  }

  @Override
  public Map<String, Type> getTypes() {
    return schemaFile.getData();
  }

  @Override
  public void close() throws Exception {
  }

}
