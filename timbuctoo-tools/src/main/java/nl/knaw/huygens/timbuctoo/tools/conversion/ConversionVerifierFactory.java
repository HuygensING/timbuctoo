package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class ConversionVerifierFactory {

  private MongoStorage mongoStorage;
  private GraphStorage graphStorage;
  private Map<String, String> oldIdNewIdMap;

  public ConversionVerifierFactory(MongoStorage mongoStorage, GraphStorage graphStorage, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T extends DomainEntity> EntityConversionVerifier createFor(Class<T> type, int revision) {
    if (Relation.class.isAssignableFrom(type)) {
      return new RelationConversionVerifier(type, mongoStorage, graphStorage, revision, oldIdNewIdMap);
    }
    return new DomainEntityConversionVerifier<T>(type, mongoStorage, graphStorage, revision);
  }
}
