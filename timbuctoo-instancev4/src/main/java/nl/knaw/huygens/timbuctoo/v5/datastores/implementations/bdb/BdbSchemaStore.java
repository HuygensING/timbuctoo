package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaUpdateException;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.UNKNOWN;

public class BdbSchemaStore implements SchemaStore, OptimizedPatchListener {
  private static final Logger LOG = LoggerFactory.getLogger(BdbSchemaStore.class);
  private static final Function<String, Type> TYPE_MAKER = Type::new;

  private static ObjectMapper objectMapper = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .registerModule(new Jdk8Module())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);
  private final DataStorage dataStore;

  Map<String, Type> types = new HashMap<>();
  private ImportStatus importStatus;

  public BdbSchemaStore(DataStorage dataStore, ImportStatus importStatus) throws IOException {

    this.dataStore = dataStore;
    final String storedValue = this.dataStore.getValue();
    if (storedValue != null) {
      types = objectMapper.readValue(storedValue, new TypeReference<Map<String, Type>>() {});
    }
    this.importStatus = importStatus;
  }

  @Override
  public Map<String, Type> getTypes() {
    return types;
  }

  @Override
  public void close() {
    this.finish();
    try {
      this.dataStore.close();
    } catch (Exception e) {
      LOG.error("Exception while closing BdbSchemaStore", e);
    }
  }

  @Override
  public void start() {
    importStatus.setStatus("Processing entities");
    LOG.info("Processing entities");
  }

  @Override
  public void onChangedSubject(String subject, ChangeFetcher changeFetcher) {
    //Step 1: Get the types that where added, unchanged, removed
    List<Type> addedTypes = new ArrayList<>();
    List<Type> removedTypes = new ArrayList<>();
    List<Type> unchangedTypes = new ArrayList<>();
    try (Stream<CursorQuad> subjTypes = changeFetcher.getPredicates(subject, RDF_TYPE, OUT, true, true, true)) {
      subjTypes.forEach(type -> {
        boolean hadTypesBefore = false;
        final Type typeObj = types.computeIfAbsent(type.getObject(), TYPE_MAKER);
        if (type.getChangeType() == ChangeType.ASSERTED) {
          typeObj.registerSubject(1);
          addedTypes.add(typeObj);
        } else if (type.getChangeType() == ChangeType.RETRACTED) {
          hadTypesBefore = true;
          typeObj.registerSubject(-1);
          removedTypes.add(typeObj);
        } else if (type.getChangeType() == ChangeType.UNCHANGED) {
          hadTypesBefore = true;
          unchangedTypes.add(typeObj);
        }
        if (!hadTypesBefore) {
          try (Stream<CursorQuad> predicates = changeFetcher.getPredicates(subject, true, true, false)) {
            boolean subjectIsNew = !predicates.findAny().isPresent();
            if (!subjectIsNew) {
              final Type unknown = types.computeIfAbsent(UNKNOWN, TYPE_MAKER);
              removedTypes.add(unknown);
              unknown.registerSubject(-1);
            }
          }
        }
      });
    }

    if (addedTypes.isEmpty() && unchangedTypes.isEmpty()) {
      //subject currently has no types
      if (removedTypes.isEmpty()) {
        //subject had no types either
        try (Stream<CursorQuad> predicates = changeFetcher.getPredicates(subject, true, true, false)) {
          boolean subjectIsNew = !predicates.findAny().isPresent();
          if (subjectIsNew) {
            final Type unknown = types.computeIfAbsent(UNKNOWN, TYPE_MAKER);
            addedTypes.add(unknown);
            unknown.registerSubject(1);
          } else {
            unchangedTypes.add(types.computeIfAbsent(UNKNOWN, TYPE_MAKER));
          }
        }
      } else {
        //subject has become unknown
        final Type unknown = types.computeIfAbsent(UNKNOWN, TYPE_MAKER);
        addedTypes.add(unknown);
        unknown.registerSubject(1);
      }
    }

    //Step 2: loop over all predicates of the subject
    //all added types need to get an inc for the current state's predicates (unchanged + asserted)
    //all unchanged types need to get an inc for the asserted predicates and a dec for the retracted predicates
    //all removed types need to get a dec for the previous state's predicates (unchanged + retracted)

    //we stream the subjects, so we need to flip that around
    //it it was retracted -> remove it from the unchanged types and the removed types
    //if it was unchanged -> remove it from the removed types and add it to the added types
    //it it was asserted -> add it to the unchanged types and to the added types
    try (Stream<CursorQuad> predicates = changeFetcher.getPredicates(subject, true, true, true)) {
      String prevPred = "";
      Direction[] prevDir = new Direction[] { null };
      int retractedCount = 0;
      int assertedCount = 0;
      int unchangedCount = 0;

      //Predicates are a sorted list. So as soon as the predicate is different from the prevPredicate we have seen all
      //quads of that predicate.

      //so updatePredicateOccurrence is called once the predicate changes and gets the values of the _prevPredicate_
      //(which is by then completely up to date)

      //when we see the first predicate the prevPredicate is still empty so we ignore it in that case (happens inside
      //updatePredicateOccurrence)
      for (CursorQuad quad : (Iterable<CursorQuad>) predicates::iterator) {
        boolean predicateSameAsPrev = prevPred.equals(quad.getPredicate()) && prevDir[0] == quad.getDirection();
        if (!predicateSameAsPrev) {
          updatePredicateOccurrence(
            addedTypes,
            removedTypes,
            unchangedTypes,
            retractedCount,
            unchangedCount,
            assertedCount,
            prevPred,
            prevDir[0]
          );
          prevPred = quad.getPredicate();
          prevDir[0] = quad.getDirection();
          retractedCount = 0;
          assertedCount = 0;
          unchangedCount = 0;
        }
        if (quad.getChangeType() == ChangeType.RETRACTED) {
          retractedCount++;
        } else if (quad.getChangeType() == ChangeType.UNCHANGED) {
          unchangedCount++;
        } else if (quad.getChangeType() == ChangeType.ASSERTED) {
          assertedCount++;
        }
        if (quad.getDirection() != Direction.IN) {
          if (quad.getChangeType() == ChangeType.RETRACTED) {
            for (Type type : unchangedTypes) {
              updatePredicateType(type, quad, false, changeFetcher);
            }
            for (Type type : removedTypes) {
              updatePredicateType(type, quad, false, changeFetcher);
            }
          } else if (quad.getChangeType() == ChangeType.UNCHANGED) {
            for (Type type : removedTypes) {
              updatePredicateType(type, quad, false, changeFetcher);
            }
            for (Type type : addedTypes) {
              updatePredicateType(type, quad, true, changeFetcher);
            }
          } else if (quad.getChangeType() == ChangeType.ASSERTED) {
            for (Type type : unchangedTypes) {
              updatePredicateType(type, quad, true, changeFetcher);
            }
            for (Type type : addedTypes) {
              updatePredicateType(type, quad, true, changeFetcher);
            }
          }
        }
      }

      updatePredicateOccurrence(
        addedTypes,
        removedTypes,
        unchangedTypes,
        retractedCount,
        unchangedCount,
        assertedCount,
        prevPred,
        prevDir[0]
      );
    }
  }

  @Override
  public void notifyUpdate() {
    final long totalPredicateCount = types
      .values().stream()
      .flatMap(t -> t.getPredicates().stream())
      .mapToLong(p -> p.getValueTypes().values().size() + p.getReferenceTypes().values().size())
      .sum();
    LOG.info("types-size is: " + totalPredicateCount + "");
    importStatus.setStatus("types-size is: " + totalPredicateCount);
  }

  public void updatePredicateOccurrence(List<Type> addedTypes, List<Type> removedTypes, List<Type> unchangedTypes,
                                        int retractedCount, int unchangedCount, int assertedCount, String predicate,
                                        Direction direction) {
    if (!predicate.isEmpty()) {
      boolean wasList = (retractedCount + unchangedCount) > 1;
      boolean isList = (unchangedCount + assertedCount) > 1;
      boolean wasPresent = (retractedCount + unchangedCount) > 0;
      boolean isPresent = (unchangedCount + assertedCount) > 0;
      for (Type type : removedTypes) {
        setPredicateOccurrence(
          type,
          predicate,
          direction,
          wasList ? -1 : 0,
          wasPresent ? -1 : 0
        );
      }
      for (Type type : unchangedTypes) {
        setPredicateOccurrence(
          type,
          predicate,
          direction,
          wasList == isList ? 0 : isList ? 1 : -1,
          wasPresent == isPresent ? 0 : isPresent ? 1 : -1
        );
      }
      for (Type type : addedTypes) {
        setPredicateOccurrence(
          type,
          predicate,
          direction,
          isList ? 1 : 0,
          isPresent ? 1 : 0
        );
      }
    }
  }

  public void updatePredicateType(Type type, CursorQuad quad, boolean inc, ChangeFetcher changeFetcher) {
    final Predicate predicate = type.getOrCreatePredicate(quad.getPredicate(), quad.getDirection());
    if (quad.getValuetype().isPresent()) {
      predicate.incValueType(quad.getValuetype().get(), inc ? 1 : -1);
    } else {
      try (Stream<CursorQuad> typeQs = changeFetcher.getPredicates(quad.getObject(), RDF_TYPE, OUT, !inc, true, inc)) {
        boolean[] hadType = new boolean[] { false };
        typeQs.forEach(typeQ -> {
          hadType[0] = true;
          predicate.incReferenceType(typeQ.getObject(), inc ? 1 : -1);
        });
        if (!hadType[0]) {
          predicate.incReferenceType(UNKNOWN, inc ? 1 : -1);
        }
      }
    }
  }

  public void setPredicateOccurrence(Type type, String predicateUri, Direction direction, int listMutation,
                                     int subjectMutation) {
    final Predicate predicate = type.getOrCreatePredicate(predicateUri, direction);
    predicate.registerListOccurrence(listMutation);
    predicate.registerSubject(subjectMutation);
  }

  @Override
  public void finish() {
    LOG.info("Finished processing entities");
    importStatus.setStatus("Finished processing entities");
    //Step 3: Add type information to inverse predicates
    for (Map.Entry<String, Type> typeEntry : types.entrySet()) {
      Type type = typeEntry.getValue();
      String typeName = typeEntry.getKey();

      for (Predicate predicate : type.getPredicates()) {
        predicate.finish();
        if (predicate.getDirection() == Direction.IN) {
          continue;
        }
        for (String referenceType : predicate.getReferenceTypes().keySet()) {
          try {
            types.get(referenceType)
              .getPredicate(predicate.getName(), Direction.IN) //There must be an inverse for each outward predicate
              .incReferenceType(type.getName(), 1);
          } catch (Exception e) {
            String cause = "Referenced type " + referenceType + " not found";
            try {
              if (types.containsKey(referenceType)) {
                cause = "type does not have the inverse predicate " + predicate.getName();
                if (types.get(referenceType).getPredicate(predicate.getName(), Direction.IN) != null) {
                  cause = "Something failed during addreferencetype(" + typeName + ")";
                }
              }
              LOG.error("Error during inverse generation (ignored): " + cause , e);
              importStatus.addError("Error during inverse generation (ignored): " + cause, e);
            } catch (Exception e2) {
              LOG.error("Error during inverse generation " + cause, e);
              importStatus.addError("Error during inverse generation " + cause, e);
              LOG.error("Error during recovery generation ", e2);
              importStatus.addError("Error during recovery generation ", e2);
            }
          }
        }
      }
    }
    try {
      try {
        String serializedValue = objectMapper.writeValueAsString(types);
        dataStore.setValue(serializedValue);
        dataStore.commit();
      } catch (IOException | DatabaseWriteException e) {
        throw new SchemaUpdateException(e);
      }
    } catch (SchemaUpdateException e) {
      e.printStackTrace();
    }
  }
}
