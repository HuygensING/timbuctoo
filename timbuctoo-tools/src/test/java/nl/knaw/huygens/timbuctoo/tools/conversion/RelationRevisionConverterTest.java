package nl.knaw.huygens.timbuctoo.tools.conversion;

import static nl.knaw.huygens.timbuctoo.tools.conversion.RelationMatcher.likeRelation;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.tinkerpop.blueprints.Vertex;

public class RelationRevisionConverterTest {
  private static final Class<SubARelation> VARIANT_TYPE2 = SubARelation.class;
  private static final Class<Relation> VARIANT_TYPE1 = Relation.class;
  private static final int REVISION = 1;
  private static final String OLD_REL_TYPE_ID = "oldRelTypeId";
  private static final String OLD_TARGET_ID = "oldTargetId";
  private static final String NEW_TARGET_ID = "newTargetId";
  private static final String OLD_ID = "oldId";
  private static final String NEW_ID = "newId";
  private static final String REGULAR_NAME = "regularName";
  private static final String NEW_SOURCE_ID = "newSourceId";
  private static final String OLD_SOURCE_ID = "oldSourceId";
  private RelationVariantConverter variantConverter;
  private Edge edge;
  private VertexFinder vertexFinder;
  private Map<String, String> oldIdNewIdMap;
  private RelationRevisionConverter instance;
  private MongoConversionStorage mongoStorage;
  private ConversionVerifierFactory verifierFactory;

  @Before
  public void setup() {
    verifierFactory = mock(ConversionVerifierFactory.class);
    vertexFinder = mock(VertexFinder.class);
    variantConverter = mock(RelationVariantConverter.class);
    mongoStorage = mock(MongoConversionStorage.class);
    edge = mock(Edge.class);
    setupOldIdNewIdMap();

    instance = new RelationRevisionConverter(variantConverter, vertexFinder, oldIdNewIdMap, mongoStorage, verifierFactory);
  }

  private void setupOldIdNewIdMap() {
    oldIdNewIdMap = Maps.newHashMap();
    oldIdNewIdMap.put(OLD_TARGET_ID, NEW_TARGET_ID);
    oldIdNewIdMap.put(OLD_SOURCE_ID, NEW_SOURCE_ID);
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

    Vertex sourceVertex = findVertexById(NEW_SOURCE_ID);
    Vertex targetVertex = findVertexById(NEW_TARGET_ID);

    when(sourceVertex.addEdge(REGULAR_NAME, targetVertex)).thenReturn(edge);

    EntityConversionVerifier verifier1 = createVerifierFor(VARIANT_TYPE1, REVISION);
    EntityConversionVerifier verifier2 = createVerifierFor(VARIANT_TYPE2, REVISION);

    // action
    instance.convert(OLD_ID, NEW_ID, variations, REVISION);

    // verify
    verify(variantConverter).addToEdge(argThat(is(edge)), argThat(likeRelation().ofType(VARIANT_TYPE1)));
    verify(variantConverter).addToEdge(argThat(is(edge)), argThat(likeRelation().ofType(VARIANT_TYPE2)));

    verify(verifier1).verifyConversion(OLD_ID, NEW_ID);
    verify(verifier2).verifyConversion(OLD_ID, NEW_ID);

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
    when(vertexFinder.getLatestVertexById(id)).thenReturn(vertex);

    return vertex;
  }
}
