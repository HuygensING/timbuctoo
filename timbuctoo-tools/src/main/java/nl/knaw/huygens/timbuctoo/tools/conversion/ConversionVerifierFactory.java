package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class ConversionVerifierFactory {

  private MongoStorage mongoStorage;
  private GraphStorage graphStorage;

  public ConversionVerifierFactory(MongoStorage mongoStorage, GraphStorage graphStorage) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
  }

  public <T extends DomainEntity> EntityConversionVerifier createFor(Class<T> type, int revision) {
    return new DomainEntityConversionVerifier<T>(type, mongoStorage, graphStorage, revision);
  }
}
