package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class RevisionConverter {

  private Graph graph;
  private VariationConverter variationConverter;
  private ConversionVerifierFactory verifierFactory;

  public RevisionConverter(Graph graph, MongoStorage mongoStorage, GraphStorage graphStorage, TypeRegistry typeRegistry) {
    this(graph, new VariationConverter(typeRegistry), new ConversionVerifierFactory(mongoStorage, graphStorage));
  }

  public RevisionConverter(Graph graph, VariationConverter variationConverter, ConversionVerifierFactory verifierFactory) {
    this.graph = graph;
    this.variationConverter = variationConverter;
    this.verifierFactory = verifierFactory;

  }

  public <T extends DomainEntity> void convert(String oldId, String newId, List<T> variations, int revision) throws IllegalArgumentException, IllegalAccessException, StorageException {
    Vertex vertex = graph.addVertex(null);
    List<Class<? extends DomainEntity>> variantTypes = Lists.newArrayList();
    for (T variant : variations) {
      variant.setId(newId);
      variationConverter.addDataToVertex(vertex, variant);
      variantTypes.add(variant.getClass());
    }

    for (Class<? extends DomainEntity> type : variantTypes) {
      EntityConversionVerifier verifier = verifierFactory.createFor(type, revision);
      verifier.verifyConversion(oldId, newId);
    }

  }
}
