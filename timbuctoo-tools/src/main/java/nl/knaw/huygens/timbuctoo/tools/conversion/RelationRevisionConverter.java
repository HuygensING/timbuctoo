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
  private MongoStorage mongoStorage;
  private ConversionVerifierFactory verifierFactory;
  private Map<String, Object> oldIdLatestVertexIdMap;
  private Graph graph;
  private Map<String, String> oldIdNewIdMap;

  public RelationRevisionConverter(Graph graph, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap,
      Map<String, Object> oldIdLatestVertexIdMap) {
    this(new RelationVariationConverter(typeRegistry), mongoStorage, graph, new ConversionVerifierFactory(mongoStorage, graphStorage, graph, oldIdNewIdMap), oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

  RelationRevisionConverter(RelationVariationConverter variantConverter, MongoStorage mongoStorage, Graph graph, ConversionVerifierFactory verifierFactory, Map<String, String> oldIdNewIdMap,
      Map<String, Object> oldIdLatestVertexIdMap) {
    this.variantConverter = variantConverter;
    this.mongoStorage = mongoStorage;
    this.graph = graph;
    this.verifierFactory = verifierFactory;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.oldIdLatestVertexIdMap = oldIdLatestVertexIdMap;
  }

  public void convert(String oldId, String newId, List<Relation> variants, int revision) throws StorageException, IllegalArgumentException, IllegalAccessException {
    Edge edge = null;
    Object edgeId = null;
    List<Class<? extends Relation>> variantTypes = Lists.newArrayList();
    String newRelationTypeId = null;
    for (Relation variant : variants) {
      if (edge == null) {
        newRelationTypeId = getNewRelationTypeId(variant);
        edge = createEdge(variant);
        edgeId = edge.getId();
      }

      variant.setId(newId);
      variant.setTypeId(newRelationTypeId);
      variantConverter.addToEdge(edge, variant);
      variantTypes.add(variant.getClass());
    }

    checkIfVariantsAreCorrectlyStored(oldId, newId, edgeId, revision, variantTypes);

  }

  private String getNewRelationTypeId(Relation variant) {

    return oldIdNewIdMap.get(variant.getTypeId());
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
    Object vertexId = oldIdLatestVertexIdMap.get(oldId);

    return graph.getVertex(vertexId);
  }
}
