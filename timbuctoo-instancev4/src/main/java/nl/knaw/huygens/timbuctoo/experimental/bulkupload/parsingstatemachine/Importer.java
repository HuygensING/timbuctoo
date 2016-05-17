package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.Saver;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Importer {
  private Set<Integer> idsToSkip = new HashSet<>();
  private ImportPropertyDescriptions properties;
  private final Saver saver;
  private Collection currentCollection;
  private String[] currentProperties;
  private Optional<String> curUniqueIdentifier = Optional.empty();
  private boolean hasIdentifierColumn = false;

  private ImportState currentState = ImportState.NOTHING;

  public Importer(Saver saver) {
    this.saver = saver;
  }

  public Result startCollection(String collectionName) {
    if (currentState != ImportState.NOTHING) {
      return Result.failure("I was not expecting a collection declaration here");
    }
    final Vre vre = saver.getVre();
    final Optional<Collection> collectionOpt = vre.getCollectionForCollectionName(collectionName);
    if (collectionOpt.isPresent()) {
      currentCollection = collectionOpt.get();
      currentState = ImportState.GETTING_DECLARATION;
      properties = new ImportPropertyDescriptions(currentCollection);
      hasIdentifierColumn = false;
      return Result.success();
    } else {
      currentState = ImportState.SKIPPING;
      if (collectionName.equals("")) {
        return Result.failure("This collection has no name!");
      } else {
        return Result.failure("Collection " + collectionName + " does not exist in " + vre.getVreName());
      }
    }
  }

  public Result registerPropertyName(int id, String name) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a property declaration here");
    }
    if (currentCollection.getWriteableProperties().containsKey(name)) {
      properties.getOrCreate(id).setPropertyName(name);
      return Result.success();
    } else {
      idsToSkip.add(id);
      return Result.failure("Collection " + currentCollection.getCollectionName() + " has no property configured " +
                              "with name " + name);
    }
  }

  public Result registerUnique(int id, boolean isUnique) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a uniqueness declaration here");
    }
    if (hasIdentifierColumn) {
      return Result.failure("Only one identifier column per collection is possible");
    } else {
      hasIdentifierColumn = true;
      properties.getOrCreate(id).setUnique(isUnique);
    }
    return Result.success();
  }

  public Result registerRelationName(int id, String relationName) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a relation name here");
    }
    properties.getOrCreate(id).setRelationName(relationName);
    return Result.success();
  }

  public Result registerTargetCollection(int id, String targetCollection) {
    if (idsToSkip.contains(id) || currentState == ImportState.SKIPPING) {
      return Result.ignored();
    }
    if (currentState != ImportState.GETTING_DECLARATION) {
      return Result.failure("I was not expecting a target collection name here");
    }
    properties.getOrCreate(id).setTargetCollection(targetCollection);
    return Result.success();
  }

  public void startEntity() {
    if (currentState == ImportState.GETTING_DECLARATION) {
      //FIXME check for no mixed relation/property specifications
      currentState = ImportState.GETTING_VALUES;
    }

    currentProperties = new String[properties.getPropertyCount()];
    curUniqueIdentifier = Optional.empty();
  }

  public Result setValue(int id, String value) {
    if (currentState != ImportState.GETTING_VALUES) {
      return Result.failure("I was not expecting a value property here");
    }
    Optional<ImportPropertyDescription> propOpt = properties.get(id);
    if (propOpt.isPresent()) {
      ImportPropertyDescription prop = propOpt.get();
      if (prop.isUnique()) {
        curUniqueIdentifier = Optional.of(value);
      }
      currentProperties[prop.getOrder()] = value;
      return Result.ignored();//actual validation will happen during finishEntity
    } else {
      return Result.failure("No property declared for id " + id);
    }
  }

  public HashMap<Integer, Result> finishEntity() {
    HashMap<String, Object> propertyValues = new HashMap<>();
    HashMap<ImportPropertyDescription, String> relations = new HashMap<>();
    HashMap<Integer, Result> results = new HashMap<>();
    for (int i = 0, currentPropertiesLength = currentProperties.length; i < currentPropertiesLength; i++) {
      String value = currentProperties[i];
      if (value != null) {
        ImportPropertyDescription desc = properties.getByOrder(i);
        if (desc.isProperty()) {
          propertyValues.put(desc.getPropertyName(), value); //FIXME transform value and put error in results
          results.put(desc.getId(), Result.success());
        } else {
          relations.put(desc, value);
        }
      }
    }

    //FIXME: fail if the vertex has already had his properties set
    Vertex vertex = saver.setVertexProperties(currentCollection, curUniqueIdentifier, propertyValues);

    for (Map.Entry<ImportPropertyDescription, String> entry : relations.entrySet()) {
      final ImportPropertyDescription prop = entry.getKey();
      final String value = entry.getValue();
      final Optional<String> result = saver.makeRelation(vertex, prop.getRelationName(), currentCollection,
                                                         prop.getTargetCollection(), value);
      if (result.isPresent()) {
        results.put(prop.getId(), Result.failure(result.get()));
      } else {
        results.put(prop.getId(), Result.success());
      }
    }

    return results;
  }

  public Result finishCollection() {
    currentState = ImportState.NOTHING;
    Collection prevCollection = currentCollection;
    currentCollection = null;
    return toResult(saver.checkLeftoverVerticesThatWereExpected(prevCollection));
  }

  public Result finishImport() {
    return toResult(saver.checkLeftoverCollectionsThatWereExpected())
      .and(toResult(saver.checkRelationtypesThatWereExpected()));
  }

  private Result toResult(Optional<String> failure) {
    return failure.map(Result::failure).orElse(Result.success());
  }

  private enum ImportState {
    NOTHING,
    GETTING_DECLARATION,
    GETTING_VALUES,
    SKIPPING
  }
}
