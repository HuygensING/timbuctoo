package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class RelationRevisionConverter {

  private RelationVariationConverter variantConverter;
  private VertexFinder vertexFinder;
  private Map<String, String> oldIdNewIdMap;
  private MongoStorage mongoStorage;
  private ConversionVerifierFactory verifierFactory;

  public RelationRevisionConverter(Graph graph, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap) {
    this(new RelationVariationConverter(typeRegistry), new VertexFinder(graph), oldIdNewIdMap, mongoStorage, new ConversionVerifierFactory(mongoStorage, graphStorage, graph, oldIdNewIdMap));
  }

  RelationRevisionConverter(RelationVariationConverter variantConverter, VertexFinder vertexFinder, Map<String, String> oldIdNewIdMap, MongoStorage mongoStorage,
      ConversionVerifierFactory verifierFactory) {
    this.variantConverter = variantConverter;
    this.vertexFinder = vertexFinder;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.mongoStorage = mongoStorage;
    this.verifierFactory = verifierFactory;
  }

  public void convert(String oldId, String newId, List<Relation> variants, int revision) throws StorageException, IllegalArgumentException, IllegalAccessException {
    Edge edge = null;
    Object edgeId = null;
    List<Class<? extends Relation>> variantTypes = Lists.newArrayList();
    for (Relation variant : variants) {
      variant.setId(newId);
      if (edge == null) {
        edge = createEdge(variant);
        edgeId = edge.getId();
      }

      variantConverter.addToEdge(edge, variant);
      variantTypes.add(variant.getClass());
    }

    checkIfVariantsAreCorrectlyStored(oldId, newId, edgeId, revision, variantTypes);

  }

  private void checkIfVariantsAreCorrectlyStored(String oldId, String newId, Object edgeId, int revision, List<Class<? extends Relation>> variantTypes) throws StorageException, IllegalAccessException {
    for (Class<? extends DomainEntity> type : variantTypes) {
      EntityConversionVerifier verifier = verifierFactory.createFor(type, revision);
      verifier.verifyConversion(oldId, newId, edgeId);
    }
  }

  private Edge createEdge(Relation variant) throws StorageException {
    Vertex source = getVertex(variant.getSourceId());
    Vertex target = getVertex(variant.getTargetId());
    String typeName = getRelationTypeName(variant.getTypeId());

    return source.addEdge(typeName, target);
  }

  private String getRelationTypeName(String typeId) throws StorageException {
    return mongoStorage.getEntity(RelationType.class, typeId).getRegularName();
  }

  private Vertex getVertex(String oldId) {
    String newId = oldIdNewIdMap.get(oldId);

    return vertexFinder.getLatestVertexById(newId);
  }

}
