package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityConverter {
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;

  public DomainEntityConverter(MongoConversionStorage mongoStorage, IdGenerator idGenerator, Graph graph, GraphStorage graphStorage, TypeRegistry typeRegistry) {
    this(mongoStorage, idGenerator, new RevisionConverter(graph, mongoStorage, graphStorage, typeRegistry));
  }

  DomainEntityConverter(MongoConversionStorage mongoStorage, IdGenerator idGenerator, RevisionConverter revisionConverter) {
    this.mongoStorage = mongoStorage;
    this.idGenerator = idGenerator;
    this.revisionConverter = revisionConverter;
  }

  public <T extends DomainEntity> String convert(Class<T> type, String oldId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    String newId = idGenerator.nextIdFor(type);
    AllVersionVariationMap<T> versions = mongoStorage.getAllVersionVariationsMapOf(type, oldId);

    for (Integer revision : versions.revisionsInOrder()) {
      revisionConverter.convert(oldId, newId, versions.get(revision), revision);
    }

    return newId;
  }
}
