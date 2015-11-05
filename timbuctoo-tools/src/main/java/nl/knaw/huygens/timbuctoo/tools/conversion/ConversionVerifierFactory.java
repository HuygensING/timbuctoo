package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.tinkerpop.blueprints.Graph;

public class ConversionVerifierFactory {

  private MongoStorage mongoStorage;
  private TinkerPopConversionStorage graphStorage;
  private Map<String, String> oldIdNewIdMap;
  private Graph graph;

  public ConversionVerifierFactory(MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, Graph graph, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.graph = graph;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T extends DomainEntity> EntityConversionVerifier createFor(Class<T> type, int revision) {
    if (Relation.class.isAssignableFrom(type)) {
      return new RelationConversionVerifier(type, mongoStorage, graphStorage, graph, revision, oldIdNewIdMap);
    }
    return new DomainEntityConversionVerifier<T>(type, mongoStorage, graphStorage, revision);
  }
}
