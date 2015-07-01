package nl.knaw.huygens.timbuctoo.tools.conversion;

import static nl.knaw.huygens.timbuctoo.tools.conversion.RelationMatcher.likeRelation;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class RelationRevisionConverterTest {
  private static final String NEW_REL_TYPE_ID = "typeId";
  private static final String LATEST_SOURCE_VERTEX_ID = "latestSourceVertexID";
  private static final String LATEST_TARGET_VERTEX_ID = "latestTargetVertexId";
  private static final Class<SubARelation> VARIANT_TYPE2 = SubARelation.class;
  private static final Class<Relation> VARIANT_TYPE1 = Relation.class;
  private static final int REVISION = 1;
  private static final String OLD_REL_TYPE_ID = "oldRelTypeId";
  private static final String OLD_TARGET_ID = "oldTargetId";
  private static final String NEW_TARGET_ID = "newTargetId";
  private static final String OLD_ID = "oldId";
  private static final String NEW_ID = "newId";
  private static final Object NEW_INTERNAL_ID = "newInternalId";
  private static final String REGULAR_NAME = "regularName";
  private static final String NEW_SOURCE_ID = "newSourceId";
  private static final String OLD_SOURCE_ID = "oldSourceId";
  private RelationVariationConverter variantConverter;
  private Edge edge;
  private Map<String, String> oldIdNewIdMap;
  private RelationRevisionConverter instance;
  private MongoConversionStorage mongoStorage;
  private ConversionVerifierFactory verifierFactory;
  private HashMap<String, Object> oldIdLatestVertexIdMap;
  private Graph graph;
  private Vertex sourceVertex;
  private Vertex targetVertex;

  @Before
  public void setup() {
    graph = mock(Graph.class);
    verifierFactory = mock(ConversionVerifierFactory.class);
    variantConverter = mock(RelationVariationConverter.class);
    mongoStorage = mock(MongoConversionStorage.class);
    edge = mock(Edge.class);
    when(edge.getId()).thenReturn(NEW_INTERNAL_ID);

    setupOldIdNewIdMap();
    setupOldIdLatestVertexIdMap();
    setupVertices();

    instance = new RelationRevisionConverter(variantConverter, mongoStorage, graph, verifierFactory, oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

  private void setupVertices() {
    sourceVertex = findVertexById(LATEST_SOURCE_VERTEX_ID);
    targetVertex = findVertexById(LATEST_TARGET_VERTEX_ID);
  }

  private void setupOldIdLatestVertexIdMap() {
    oldIdLatestVertexIdMap = Maps.newHashMap();
    oldIdLatestVertexIdMap.put(OLD_TARGET_ID, LATEST_TARGET_VERTEX_ID);
    oldIdLatestVertexIdMap.put(OLD_SOURCE_ID, LATEST_SOURCE_VERTEX_ID);
  }

  private void setupOldIdNewIdMap() {
    oldIdNewIdMap = Maps.newHashMap();
    oldIdNewIdMap.put(OLD_TARGET_ID, NEW_TARGET_ID);
    oldIdNewIdMap.put(OLD_SOURCE_ID, NEW_SOURCE_ID);
    oldIdNewIdMap.put(OLD_REL_TYPE_ID, NEW_REL_TYPE_ID);
  }

  @Test
  public void convertAddsEachVariantToTheNewlyCreatedEdge() throws Exception {
    List<Relation> variations = Lists.newArrayList();
    Relation variant1 = new Relation();
    variant1.setTargetId(OLD_TARGET_ID);
    variant1.setSourceId(OLD_SOURCE_ID);
    variant1.setTypeId(OLD_REL_TYPE_ID);
    variations.add(variant1);

    SubARelation variant2 = new SubARelation();
    variations.add(variant2);

    findRelationTypeIdGivesName(OLD_REL_TYPE_ID, REGULAR_NAME);

    when(sourceVertex.addEdge(REGULAR_NAME, targetVertex)).thenReturn(edge);

    EntityConversionVerifier verifier1 = createVerifierFor(VARIANT_TYPE1, REVISION);
    EntityConversionVerifier verifier2 = createVerifierFor(VARIANT_TYPE2, REVISION);

    // action
    instance.convert(OLD_ID, NEW_ID, variations, REVISION);

    // verify
    verify(variantConverter).addToEdge(argThat(is(edge)), argThat(likeRelation().ofType(VARIANT_TYPE1).withId(NEW_ID).withTypeId(NEW_REL_TYPE_ID)));
    verify(variantConverter).addToEdge(argThat(is(edge)), argThat(likeRelation().ofType(VARIANT_TYPE2).withId(NEW_ID).withTypeId(NEW_REL_TYPE_ID)));

    verify(verifier1).verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);
    verify(verifier2).verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);

  }

  private EntityConversionVerifier createVerifierFor(Class<? extends Relation> type, int revision) {
    EntityConversionVerifier verifier = mock(EntityConversionVerifier.class);
    when(verifierFactory.createFor(type, revision)).thenReturn(verifier);
    return verifier;
  }

  private void findRelationTypeIdGivesName(String id, String name) throws StorageException {
    RelationType relType = new RelationType();
    relType.setRegularName(name);
    when(mongoStorage.getEntity(RelationType.class, id)).thenReturn(relType);
  }

  private Vertex findVertexById(String id) {
    Vertex vertex = mock(Vertex.class);
    when(graph.getVertex(id)).thenReturn(vertex);

    return vertex;
  }
}
