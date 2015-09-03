package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityConverterFactory {

  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private VertexDuplicator vertexDuplicator;
  private Map<String, String> oldIdNewIdMap;
  private Map<String, Object> oldIdLatestVertexIdMap;

  public DomainEntityConverterFactory(MongoConversionStorage mongoStorage, Graph graph, TypeRegistry typeRegistry, TinkerPopConversionStorage graphStorage, Map<String, String> oldIdNewIdMap,
      Map<String, Object> oldIdLatestVertexIdMap) {
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.oldIdLatestVertexIdMap = oldIdLatestVertexIdMap;
    this.idGenerator = new IdGenerator();
    this.revisionConverter = new RevisionConverter(graph, new VariationConverter(new ElementConverterFactory(typeRegistry)), new ConversionVerifierFactory(mongoStorage, graphStorage, graph,
        oldIdNewIdMap));
    this.vertexDuplicator = new VertexDuplicator(graph);
  }

  public <T extends DomainEntity> DomainEntityConverter<T> create(Class<T> type, String id) {
    return new DomainEntityConverter<T>(type, id, mongoStorage, idGenerator, revisionConverter, vertexDuplicator, oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

}
