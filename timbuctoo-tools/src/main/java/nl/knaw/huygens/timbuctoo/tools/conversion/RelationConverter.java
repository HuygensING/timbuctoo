package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import com.tinkerpop.blueprints.Graph;

public class RelationConverter {

  private static final Class<Relation> TYPE = Relation.class;
  private final MongoConversionStorage mongoStorage;
  private final RelationRevisionConverter revisionConverter;
  private final IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;

  public RelationConverter(MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap,
      IdGenerator idGenerator) {
    this(mongoStorage, new RelationRevisionConverter(graph, mongoStorage, graphStorage, typeRegistry, oldIdNewIdMap), idGenerator, oldIdNewIdMap);
  }

  RelationConverter(MongoConversionStorage mongoStorage, RelationRevisionConverter revisionConverter, IdGenerator idGenerator, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.revisionConverter = revisionConverter;
    this.idGenerator = idGenerator;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  public void convert(String oldId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    AllVersionVariationMap<Relation> map = mongoStorage.getAllVersionVariationsMapOf(TYPE, oldId);
    String newId = idGenerator.nextIdFor(TYPE);
    oldIdNewIdMap.put(oldId, newId);

    for (int revision : map.revisionsInOrder()) {
      revisionConverter.convert(oldId, newId, map.get(revision), revision);
    }
  }
}
