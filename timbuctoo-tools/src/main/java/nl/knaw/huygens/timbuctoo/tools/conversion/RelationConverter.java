package nl.knaw.huygens.timbuctoo.tools.conversion;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;

import java.util.Map;

public class RelationConverter {

  private static final Class<Relation> TYPE = Relation.class;
  private final MongoConversionStorage mongoStorage;
  private final RelationRevisionConverter revisionConverter;
  private final IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;

  public RelationConverter(MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap,
                           Map<String, Object> oldIdLatestVertexIdMap, IdGenerator idGenerator) {
    this(mongoStorage, new RelationRevisionConverter(graph, mongoStorage, graphStorage, typeRegistry, oldIdNewIdMap, oldIdLatestVertexIdMap), idGenerator, oldIdNewIdMap);
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

    Edge edge = null;
    for (int revision : map.revisionsInOrder()) {
      edge = revisionConverter.convert(oldId, newId, map.get(revision), revision);
      edge.setProperty(ElementFields.IS_LATEST, false);
    }

    edge.setProperty(ElementFields.IS_LATEST, true);
  }
}
