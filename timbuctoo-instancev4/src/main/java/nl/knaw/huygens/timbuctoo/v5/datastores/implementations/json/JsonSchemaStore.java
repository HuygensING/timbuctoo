package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.UNKNOWN;

public class JsonSchemaStore implements SchemaStore {
  private final TripleStore tripleStore;
  private Map<String, Type> types = null;
  private static final Function<String, Type> TYPE_MAKER = Type::new;
  private final JsonFileBackedData<Map<String, Type>> schemaFile;

  public JsonSchemaStore(TripleStore tripleStore, File dataLocation) throws IOException {
    this.tripleStore = tripleStore;
    schemaFile = JsonFileBackedData.getOrCreate(
      new File(dataLocation, "schema.json"),
      null,
      new TypeReference<Map<String, Type>>() {},
      types -> {
        for (Map.Entry<String, Type> typeEntry : types.entrySet()) {
          typeEntry.getValue().setName(typeEntry.getKey());
        }
        return types;
      }
    );
  }

  @Override
  public Map<String, Type> getTypes() {
    if (schemaFile.getData() == null) {
      generate();
    } else {
      return schemaFile.getData();
    }
    return types;
  }

  public void generate() {
    Map<String, Type> curTypes = new HashMap<>();
    String curSubject = "";
    String prevPredicate = "";
    boolean predicateUsedTwice;
    try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples()) {
      while (triples.hasNext()) {
        String[] triple = triples.next();
        if (!curSubject.equals(triple[0])) {
          curSubject = triple[0];
          prevPredicate = "";
          curTypes.clear();
        }
        if (RDF_TYPE.equals(triple[1])) {
          curTypes.put(triple[2], types.computeIfAbsent(triple[2], TYPE_MAKER));
        } else {
          if (curTypes.isEmpty()) {
            curTypes.put(UNKNOWN, types.computeIfAbsent(UNKNOWN, TYPE_MAKER));
          }
          //if same predicate twice in a row
          if (prevPredicate.equals(triple[1])) {
            predicateUsedTwice = true;
          } else {
            predicateUsedTwice = false;
          }
          prevPredicate = triple[1];
          for (Type type : curTypes.values()) {
            Predicate predicate = type.getOrCreatePredicate(triple[1]);
            if (predicateUsedTwice) {
              predicate.setList(true);
            } else {
              predicate.incUsage(); //this predicate is used at an instance of this type
            }
            if (triple[3] != null) {
              predicate.addValueType(triple[3]);
            } else {
              boolean hadType = false;
              try (AutoCloseableIterator<String[]> objectTypes = tripleStore
                .getTriples(triple[2], RDF_TYPE)) {
                while (objectTypes.hasNext()) {
                  hadType = true;
                  predicate.addReferenceType(objectTypes.next()[2]);
                }
              }
              if (!hadType) {
                predicate.addReferenceType(UNKNOWN);
              }
            }
          }
        }
      }
    }
    try {
      schemaFile.updateData(types -> types);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {

  }
}
