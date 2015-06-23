package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityConverterFactory {

  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private VertexDuplicator vertexDuplicator;
  private Map<String, String> oldIdNewIdMap;

  public DomainEntityConverterFactory(MongoConversionStorage mongoStorage, Graph graph, TypeRegistry typeRegistry, GraphStorage graphStorage, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.idGenerator = new IdGenerator();
    this.revisionConverter = new RevisionConverter(graph, new VariationConverter(new ElementConverterFactory(typeRegistry)), new ConversionVerifierFactory(mongoStorage, graphStorage));
    this.vertexDuplicator = new VertexDuplicator(graph);
  }

  public <T extends DomainEntity> DomainEntityConverter<T> create(Class<T> type, String id) {
    return new DomainEntityConverter<T>(type, id, mongoStorage, idGenerator, revisionConverter, vertexDuplicator, oldIdNewIdMap);
  }

  public <T extends DomainEntity> Runnable createConverterRunnable(Class<T> type, String id) {
    return new ConverterRunnable<T>(type, id, create(type, id));
  }

  private static class ConverterRunnable<T extends DomainEntity> implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(ConverterRunnable.class);
    private DomainEntityConverter<T> converter;
    private Class<T> type;
    private String id;

    public ConverterRunnable(Class<T> type, String id, DomainEntityConverter<T> converter) {
      this.type = type;
      this.id = id;
      this.converter = converter;
    }

    @Override
    public void run() {
      try {
        converter.convert();
      } catch (IllegalArgumentException | IllegalAccessException | StorageException e) {
        LOG.error("Could not convert \"{}\" with id \"{}\"", type.getSimpleName(), id);
      }
    }
  }
}
