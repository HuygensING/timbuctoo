package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore;

import com.google.common.collect.ListMultimap;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RelationPredicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.UNKNOWN;

public class SchemaEntityProcessor implements EntityProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SchemaEntityProcessor.class);
  private static final Function<String, Type> TYPE_MAKER = Type::new;
  private final SchemaUpdater schemaUpdater;
  private Map<String, Type> types = new HashMap<>();
  private int currentVersion;

  public SchemaEntityProcessor(SchemaUpdater schemaUpdater, int currentVersion) {
    this.schemaUpdater = schemaUpdater;
    this.currentVersion = currentVersion;
  }

  @Override
  public void start(int currentVersion) {
    this.currentVersion = currentVersion;
    types.clear(); // clear the types, otherwise the schema will not be properly updated
    LOG.info("Processing entities");
  }

  @Override
  public void processEntity(String cursor, String subjectUri, ListMultimap<String, PredicateData> addedPredicates,
                            Map<String, Boolean> inversePredicates) {
    if (addedPredicates.size() > 100) {
      LOG.info("There's " + addedPredicates.size() + " predicates!"); //indicates that this might be a weird rdf file
    }

    //Step 1: create all the types that this subject belongs to
    Map<String, Type> curTypes = new HashMap<>();
    List<PredicateData> subjectTypes = addedPredicates.get(RDF_TYPE);
    if (subjectTypes.stream().noneMatch(p -> p instanceof RelationPredicate)) {
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

    //Step 2: Add the predicates to each type
    for (Type type : curTypes.values()) {
      //add all the inverse predicates (we ignore type information here, we just note that an inverse predicate
      //exists and whether it was a list. Retrieving all type information is wasteful because we are already
      //condensing it using the outward predicates. So we can look up all type information at the end using
      //the schema (during finish()
      for (Map.Entry<String, Boolean> inversePredicate : inversePredicates.entrySet()) {
        Predicate predicate = type.getOrCreatePredicate(inversePredicate.getKey(), Direction.IN);
        if (inversePredicate.getValue()) {
          predicate.setList(true);
        }
        predicate.incUsage();
      }

      //add all the outward predicates
      for (String predicateUri : addedPredicates.keys()) {
        final Predicate predicate = type.getOrCreatePredicate(predicateUri, Direction.OUT);
        List<PredicateData> predicateValues = addedPredicates.get(predicateUri);
        if (predicateValues.size() > 1) {
          predicate.setList(true);
        }
        predicate.incUsage();
        for (PredicateData predicateValue : predicateValues) {
          predicateValue.handle(new PredicateHandler() {
            @Override
            public void onRelation(String uri, List<String> types1) {
              if (types1.isEmpty()) {
                types.computeIfAbsent(UNKNOWN, TYPE_MAKER);
                predicate.addReferenceType(UNKNOWN);
              } else {
                for (String type1 : types1) {
                  predicate.addReferenceType(type1);
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
  public int getCurrentVersion() {
    return currentVersion;
  }

  @Override
  public void finish() {
    LOG.info("Finished processing entities");
    //Step 3: Add type information to inverse predicates
    for (Map.Entry<String, Type> typeEntry : types.entrySet()) {
      Type type = typeEntry.getValue();
      String typeName = typeEntry.getKey();

      for (Predicate predicate : type.getPredicates()) {
        if (predicate.getDirection() == Direction.IN || RdfConstants.RDF_TYPE.equals(predicate.getName())) {
          continue;
        }
        for (String referenceType : predicate.getReferenceTypes()) {
          try {
            types.get(referenceType)
              .getPredicate(predicate.getName(), Direction.IN) //There must be an inverse for each outward predicate
              .addReferenceType(typeName);
          } catch (Exception e) {
            String cause = "Referenced type not found";
            try {
              if (types.containsKey(referenceType)) {
                cause = "type does not have the inverse predicate " + predicate.getName();
                if (types.get(referenceType).getPredicate(predicate.getName(), Direction.IN) != null) {
                  cause = "Something failed during addreferencetype(" + typeName + ")";
                }
              }
              LOG.error("Error during inverse generation (ignored): " + cause , e);
            } catch (Exception e2) {
              LOG.error("Error during inverse generation " + cause, e);
              LOG.error("Error during recovery generation ", e2);
            }
          }
        }
      }
    }
    try {
      schemaUpdater.replaceSchema(types);
    } catch (SchemaUpdateException e) {
      e.printStackTrace();
    }
  }
}
