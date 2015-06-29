package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class RelationConversionVerifier<T extends Relation> extends DomainEntityConversionVerifier<T> implements EntityConversionVerifier {

  private RelationPropertyVerifier propertyVerifier;

  public RelationConversionVerifier(Class<T> type, MongoStorage mongoStorage, GraphStorage graphStorage, int revision, Map<String, String> oldIdNewIdMap) {
    super(type, mongoStorage, graphStorage, revision);
    propertyVerifier = new RelationPropertyVerifier(oldIdNewIdMap);
  }

  @Override
  protected PropertyVerifier getPropertyVerifier() {
    return propertyVerifier;
  }

  @Override
  protected T getNewItem(String newId) throws StorageException {
    T revEntity = graphStorage.getRelationRevision(type, newId, revision);
    return revEntity != null ? revEntity : graphStorage.getRelation(type, newId);
  }
}
