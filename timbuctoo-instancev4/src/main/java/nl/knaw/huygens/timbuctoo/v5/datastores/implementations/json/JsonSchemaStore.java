package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ListMultimap;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateHandler;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.UNKNOWN;

public class JsonSchemaStore implements SchemaStore {
  private static final Function<String, Type> TYPE_MAKER = Type::new;
  private final JsonFileBackedData<Map<String, Type>> schemaFile;

  public JsonSchemaStore(DataProvider dataProvider, File schemaLocation)
    throws IOException {
    schemaFile = JsonFileBackedData.getOrCreate(
      schemaLocation,
      () -> null,
      new TypeReference<Map<String, Type>>() {},
      types -> {
        for (Map.Entry<String, Type> typeEntry : types.entrySet()) {
          typeEntry.getValue().setName(typeEntry.getKey());
        }
        return types;
      }
    );
    dataProvider.subscribeToEntities(new EntityProcessor() {
      private Map<String, Type> types = new HashMap<>();

      @Override
      public void start() {

      }

      @Override
      public void processEntity(String cursor, String subjectUri, ListMultimap<String, PredicateData> addedPredicates) {
        Map<String, Type> curTypes = new HashMap<>();
        List<PredicateData> subjectTypes = addedPredicates.get(RDF_TYPE);
        //create all the types that this subject belongs to
        if (subjectTypes.isEmpty()) {
          curTypes.put(UNKNOWN, types.computeIfAbsent(UNKNOWN, TYPE_MAKER));
        } else {
          for (PredicateData type : subjectTypes) {
            type.handle(new PredicateHandler() {
              @Override
              public void onRelation(String uri, List<String> typesOfRelation) {
                curTypes.put(uri, types.computeIfAbsent(uri, TYPE_MAKER));
              }

              @Override
              public void onValue(String value, String dataType) {
              }

              @Override
              public void onLanguageTaggedString(String value, String language) {
              }
            });
          }
        }
        for (Type type : curTypes.values()) {
          for (String predicateUri : addedPredicates.keys()) {
            Predicate predicate = type.getOrCreatePredicate(predicateUri);
            List<PredicateData> predicateValues = addedPredicates.get(predicateUri);
            if (predicateValues.size() > 1) {
              predicate.setList(true);
            }
            predicate.incUsage();
            for (PredicateData predicateValue : predicateValues) {
              predicateValue.handle(new PredicateHandler() {
                @Override
                public void onRelation(String uri, List<String> types) {
                  if (types.isEmpty()) {
                    predicate.addReferenceType(UNKNOWN);
                  } else {
                    for (String type : types) {
                      predicate.addReferenceType(type);
                    }
                  }
                }

                @Override
                public void onValue(String value, String dataType) {
                  predicate.addValueType(dataType);
                }

                @Override
                public void onLanguageTaggedString(String value, String language) {
                  predicate.addValueType(RdfConstants.LANGSTRING);
                }
              });
            }
          }
        }
      }

      @Override
      public void finish() {
        try {
          schemaFile.updateData(oldTypes -> types);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, null);
  }

  @Override
  public Map<String, Type> getTypes() {
    return schemaFile.getData();
  }

  @Override
  public void close() throws Exception {
  }
}
