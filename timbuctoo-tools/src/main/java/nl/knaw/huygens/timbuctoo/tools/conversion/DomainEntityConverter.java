package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class DomainEntityConverter<T extends DomainEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityConverter.class);

  private final Class<T> type;
  private final Graph graph;
  private final GraphStorage graphStorage;
  private final IdGenerator idGenerator;

  private final ElementConverterFactory converterFactory;
  private final MongoConversionStorage mongoStorage;
  private final Map<String, String> oldIdNewIdMap;

  public DomainEntityConverter(Class<T> type, Graph graph, GraphStorage graphStorage, IdGenerator idGenerator, ElementConverterFactory converterFactory, MongoConversionStorage mongoStorage,
      Map<String, String> oldIdNewIdMap) {
    this.type = type;
    this.graph = graph;
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.converterFactory = converterFactory;
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;

  }

  public void convert() throws StorageException, IllegalAccessException {
    LOG.info("Converting {}", type.getSimpleName());

    for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
      T entity = iterator.next();
      String oldId = entity.getId();
      String newId = mapOldIdtoNewId(type, entity);

      AllVersionVariationMap<T> versions = mongoStorage.getAllVersionVariationsMapOf(type, oldId);
      for (Integer revision : versions.revisionsInOrder()) {

        addVariationsToVertex(type, versions.get(revision), oldId, newId, revision);
      }
    }

    System.out.println();
  }

  private <U extends DomainEntity> void addVariationsToVertex(Class<U> type, List<T> allVariations, String oldId, String newId, Integer revision) throws ConversionException, StorageException,
      IllegalAccessException {
    List<Class<? extends DomainEntity>> variantTypes = Lists.newArrayList();

    Vertex vertex = graph.addVertex(null);

    for (T variant : allVariations) {
      variant.setId(newId);
      addVariantToVertex(vertex, variant);
      variantTypes.add(variant.getClass());
    }

    verifyConversion(variantTypes, oldId, newId, revision);
  }

  private void verifyConversion(List<Class<? extends DomainEntity>> variantTypes, String oldId, String newId, Integer revision) throws IllegalArgumentException, IllegalAccessException,
      StorageException {
    for (Class<? extends DomainEntity> type : variantTypes) {
      DomainEntityConversionVerifier<? extends DomainEntity> conversionVerifier = getEntityConverter(type, revision);
      conversionVerifier.verifyConversion(oldId, newId);
    }
  }

  private <U extends DomainEntity> DomainEntityConversionVerifier<U> getEntityConverter(Class<U> type, int revision) {
    return new DomainEntityConversionVerifier<U>(type, mongoStorage, graphStorage, revision);
  }

  private <U extends DomainEntity> void addVariantToVertex(Vertex vertex, U variant) throws ConversionException {
    @SuppressWarnings("unchecked")
    Class<U> type = (Class<U>) variant.getClass();
    VertexConverter<U> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, variant);
  }

  private <U extends Entity> String mapOldIdtoNewId(Class<U> type, U entity) {
    String oldId = entity.getId();

    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }
}
