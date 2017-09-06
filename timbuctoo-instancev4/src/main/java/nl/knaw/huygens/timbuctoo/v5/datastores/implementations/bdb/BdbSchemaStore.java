package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaEntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaUpdateException;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaUpdater;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BdbSchemaStore implements SchemaStore {

  private static ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);
  private final DataStorage dataStore;

  Map<String, Type> types = new HashMap<>();

  public BdbSchemaStore(DataProvider dataProvider, DataStorage dataStore) throws IOException {

    this.dataStore = dataStore;
    final String storedValue = this.dataStore.getValue();
    if (storedValue != null) {
      types = objectMapper.readValue(storedValue, new TypeReference<Map<String, Type>>() {});
    }

    SchemaUpdater schemaUpdater = newSchema -> {
      try {
        if (newSchema == null) {
          newSchema = new HashMap<>();
        }
        String serializedValue = objectMapper.writeValueAsString(newSchema);
        dataStore.setValue(serializedValue);
        types = newSchema;
      } catch (IOException | DatabaseWriteException e) {
        throw new SchemaUpdateException(e);
      }
    };
    dataProvider.subscribeToEntities(new SchemaEntityProcessor(schemaUpdater, -1));
  }

  @Override
  public Map<String, Type> getTypes() {
    return types;
  }

  @Override
  public void close() throws Exception {
    dataStore.close();
  }

}
