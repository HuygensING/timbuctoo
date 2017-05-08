package nl.knaw.huygens.timbuctoo.v5.datastores.schema;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;

import java.util.Map;

public interface SchemaStore extends AutoCloseable, DataStore<TripleStore> {
  Map<String, Type> getTypes();
}
