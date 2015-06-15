package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

/**
 * A class that checks if the RelationType is correctly converted.
 */
public class RelationTypeConversionChecker {

  private final MongoStorage mongoStorage;
  private final GraphStorage graphStorage;
  private PropertyVerifier propertyVerifier;

  public RelationTypeConversionChecker(MongoStorage mongoStorage, GraphStorage graphStorage) {
    this(mongoStorage, graphStorage, new PropertyVerifier());
  }

  public RelationTypeConversionChecker(MongoStorage mongoStorage, GraphStorage graphStorage, PropertyVerifier propertyVerifier) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.propertyVerifier = propertyVerifier;

  }

  public void verifyConversion(String oldId, String newId) throws StorageException {
    Class<RelationType> type = RelationType.class;
    RelationType mongoEntity = mongoStorage.getEntity(type, oldId);
    RelationType graphEntity = graphStorage.getEntity(type, newId);

    propertyVerifier.check("created", mongoEntity.getCreated(), graphEntity.getCreated());
    propertyVerifier.check("inverseName", mongoEntity.getInverseName(), graphEntity.getInverseName());
    propertyVerifier.check("modified", mongoEntity.getModified(), graphEntity.getModified());
    propertyVerifier.check("regularName", mongoEntity.getRegularName(), graphEntity.getRegularName());
    propertyVerifier.check("rev", mongoEntity.getRev(), graphEntity.getRev());
    propertyVerifier.check("sourceTypeName", mongoEntity.getSourceTypeName(), graphEntity.getSourceTypeName());
    propertyVerifier.check("targetTypeName", mongoEntity.getTargetTypeName(), graphEntity.getTargetTypeName());
    propertyVerifier.check("derived", mongoEntity.isDerived(), graphEntity.isDerived());
    propertyVerifier.check("reflexive", mongoEntity.isReflexive(), graphEntity.isReflexive());
    propertyVerifier.check("symmetric", mongoEntity.isSymmetric(), graphEntity.isSymmetric());

    if (propertyVerifier.hasInconsistentProperties()) {
      throw new VerificationException(oldId, newId, propertyVerifier.getMismatches());
    }
  }

}
