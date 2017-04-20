package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.util.ThroughputLogger;
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
  protected final File schemaFile;
  protected final ObjectMapper mapper;

  public JsonSchemaStore(TripleStore tripleStore, File dataLocation) {
    this.tripleStore = tripleStore;
    mapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
    schemaFile = new File(dataLocation, "schema.json");
  }

  @Override
  public Map<String, Type> getTypes() {
    if (types == null) {
      if (schemaFile.exists()) {
        try {
          types = mapper.readValue(schemaFile, new TypeReference<HashMap<String, Type>>() {
          });
          for (Map.Entry<String, Type> typeEntry : types.entrySet()) {
            typeEntry.getValue().setName(typeEntry.getKey());
          }
        } catch (IOException e) {
          e.printStackTrace();
          generate();
        }
        return types;
      } else {
        generate();
      }
    }
    return types;
  }

  public void generate() {
    Map<String, Type> curTypes = new HashMap<>();
    String curSubject = "";
    ThroughputLogger throughputLogger = new ThroughputLogger(10);
    String prevPredicate = "";
    boolean predicateUsedTwice;
    try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples()) {
      while (triples.hasNext()) {
        String[] triple = triples.next();
        throughputLogger.tripleProcessed();
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
      mapper.writeValue(schemaFile, types);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {

  }
}
