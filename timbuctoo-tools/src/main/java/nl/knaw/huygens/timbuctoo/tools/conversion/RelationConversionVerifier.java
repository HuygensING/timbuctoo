package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.tinkerpop.blueprints.Graph;

public class RelationConversionVerifier<T extends Relation> extends DomainEntityConversionVerifier<T> implements EntityConversionVerifier {

  private RelationPropertyVerifier propertyVerifier;

  public RelationConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, Graph graph, int revision, Map<String, String> oldIdNewIdMap) {
    super(type, mongoStorage, graphStorage, revision);
    propertyVerifier = new RelationPropertyVerifier(oldIdNewIdMap);
  }

  @Override
  protected PropertyVerifier getPropertyVerifier() {
    return propertyVerifier;
  }

  @Override
  protected T getNewItem(Object newId) throws StorageException {

    return graphStorage.getRelationByEdgeId(type, newId);
  }
}
