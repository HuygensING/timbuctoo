package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.core.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.core.TimbuctooActionsStubs;
import nl.knaw.huygens.timbuctoo.core.dto.DataStream;
import nl.knaw.huygens.timbuctoo.core.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.core.dto.RelationType;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperations;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperationsStubs;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.rdf.TripleHelper.createSingleTriple;
import static nl.knaw.huygens.timbuctoo.rdf.TripleHelper.createTripleIterator;
import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class TripleImporterIntegrationTest {
  private static final String LOCATION_COLLECTION = "locations";
  private static final String CONCEPTS_COLLECTION = "concepts";
  private static final String VRE_NAME = "vreName";
  private static final String RELATION_COLLECTION_NAME = VRE_NAME + "relations";
  private static final String DEFAULT_COLLECTION_NAME = VRE_NAME + "unknowns";
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String ASIA_URI = "http://tl.dbpedia.org/resource/Asia";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";
  private static final String TYPE_URI = "http://www.opengis.net/gml/_Feature";
  private static final String TYPE_NAME = VRE_NAME + "_Feature";
  private static final String COLLECTION_NAME = VRE_NAME + "_Features";
  private static final String FICTIONAL_TYPE_URI = "http://www.opengis.net/gml/" + "_FictionalFeature";
  private static final String FICTIONAL_TYPE_NAME = VRE_NAME + "_FictionalFeature";
  private static final String FICTIONAL_COLLECTION_NAME = VRE_NAME + "_FictionalFeatures";
  private static final String DEFAULT_ENTITY_TYPE_NAME = VRE_NAME + "unknown";
  private static final String ABADAN_HAS_TYPE_FEATURE_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
      "<" + TYPE_URI + "> .";
  private static final String ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
      "<" + FICTIONAL_TYPE_URI + "> .";
  private static final String ABADAN_POINT_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<http://www.georss.org/georss/point> " +
      "\"30.35 48.28333333333333\"@tl .";
  private static final String ABADAN_LAT_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<http://www.w3.org/2003/01/geo/wgs84_pos#lat> " +
      "\"30.35\"^^<http://www.w3.org/2001/XMLSchema#float> .";
  private static final String IRAN_POINT_TRIPLE =
    "<" + IRAN_URI + "> " +
      "<http://www.georss.org/georss/point> " +
      "\"30.339166666666667 48.30416666666667\"@tl .";
  private static final String ABADAN_IS_PART_OF_IRAN_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + IS_PART_OF_URI + "> " +
      "<" + IRAN_URI + "> .";
  private static final String IRAN_IS_PART_OF_ASIA_TRIPLE =
    "<" + IRAN_URI + ">" +
      "<" + IS_PART_OF_URI + "> " +
      "<" + ASIA_URI + "> .";
  private static final String FEATURE_SUBCLASS_OF_LOCATION_TRIPLE =
    "<" + TYPE_URI + "> " +
      "<http://www.w3.org/2000/01/rdf-schema#subClassOf> " +
      "<http://www.example.com/location> .";
  private TimbuctooActions timbuctooActions;
  private TripleImporter instance;

  @Before
  public void setUp() throws Exception {
    // Timbuctoo needs at least an Admin VRE.
    Vres vres = new VresBuilder()
      .withVre("Admin", "", v -> v
        .withCollection("concepts", c -> c
          .withEntityTypeName("concept")
        )
        .withCollection("relations", CollectionBuilder::isRelationCollection)
        .withCollection("locations", c -> c
          .withEntityTypeName("location")
        )
      ).build();
    TinkerPopGraphManager graphWrapper = newGraph().wrap();
    TinkerPopOperations tinkerPopOperations = TinkerPopOperationsStubs.forGraphWrapper(graphWrapper);
    tinkerPopOperations.saveVre(vres.getVre("Admin"));
    timbuctooActions = TimbuctooActionsStubs.withDataStore(tinkerPopOperations);

    instance = new TripleImporter(
      graphWrapper,
      VRE_NAME,
      timbuctooActions);

    instance.prepare();
  }

  @Test
  public void prepareAddsANewVreWithARelationsCollectionToTheDatabase() {
    Vre vre = timbuctooActions.getVre(VRE_NAME);
    assertThat(vre, is(notNullValue()));
    Optional<Collection> relationsOpt = vre.getCollectionForCollectionName(RELATION_COLLECTION_NAME);
    assertThat(relationsOpt, is(present()));
  }

  @Test
  public void importTripleShouldCreateAVertexFromATripleAddedToTheUnknownCollection()
    throws Exception {
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(ABADAN_POINT_TRIPLE);

    instance.importTriple(true, tripleExtendedIterator.next());

    Optional<ReadEntity> entityOpt = getReadEntity(DEFAULT_COLLECTION_NAME, ABADAN_URI);

    assertThat(entityOpt, is(present()));
    assertThat(entityOpt.get().getTypes(), hasItem(DEFAULT_ENTITY_TYPE_NAME));
  }

  @Test
  public void importTripleShouldAddATripleDescribingAPropertyToTheExistingEntity() throws Exception {
    final String tripleString = ABADAN_POINT_TRIPLE + "\n" + ABADAN_LAT_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(true, tripleExtendedIterator.next());
    instance.importTriple(true, tripleExtendedIterator.next());

    Optional<ReadEntity> entityOpt = getReadEntity(DEFAULT_COLLECTION_NAME, ABADAN_URI);
    assertThat(entityOpt.get().getProperties(), containsInAnyOrder(
      allOf(hasProperty("name", equalTo("point")), hasProperty("value", equalTo("30.35 48.28333333333333"))),
      allOf(hasProperty("name", equalTo("lat")), hasProperty("value", equalTo("30.35")))
    ));
  }

  @Test
  public void importTripleShouldTheEntityAndItsPropertiesTheActualCollection() throws Exception {
    final Triple abadanHasTypeFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanPointTriple = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(true, abadanHasTypeFeature);
    instance.importTriple(true, abadanPointTriple);

    Optional<ReadEntity> entityOpt = getReadEntity(COLLECTION_NAME, ABADAN_URI);
    assertThat(entityOpt.get().getProperties(), contains(
      allOf(hasProperty("name", equalTo("point")), hasProperty("value", equalTo("30.35 48.28333333333333")))
    ));
  }

  @Test
  public void importTripleShouldMapToARelationBetweenTheNewSubjectAndANewObject() throws Exception {
    final String tripleString = ABADAN_IS_PART_OF_IRAN_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(true, tripleExtendedIterator.next());

    Optional<ReadEntity> abadanOpt = getReadEntity(DEFAULT_COLLECTION_NAME, ABADAN_URI);
    assertThat(abadanOpt, is(present()));
    assertThat(abadanOpt.get().getRelations(), hasItem(
      allOf(hasProperty("entityRdfUri", equalTo(IRAN_URI)), hasProperty("relationRdfUri", equalTo(IS_PART_OF_URI)))
    ));
    Optional<ReadEntity> iranOpt = getReadEntity(DEFAULT_COLLECTION_NAME, IRAN_URI);
    assertThat(iranOpt, is(present()));
    assertThat(iranOpt.get().getRelations(), hasItem(
      allOf(hasProperty("entityRdfUri", equalTo(ABADAN_URI)), hasProperty("relationRdfUri", equalTo(IS_PART_OF_URI)))
    ));
  }

  private Optional<ReadEntity> getReadEntity(String collectionName, String rdfIro) throws Exception {
    Collection collection = timbuctooActions.getCollectionMetadata(collectionName);
    return timbuctooActions.searchEntityByRdfUri(collection, rdfIro, true);
  }

  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndAnExistingObjectVertex()
    throws Exception {
    final Triple abadan = createTripleIterator(ABADAN_POINT_TRIPLE).next();
    final Triple iran = createTripleIterator(IRAN_POINT_TRIPLE).next();
    final Triple relation = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    instance.importTriple(true, abadan);
    instance.importTriple(true, iran);
    instance.importTriple(true, relation);

    Collection collectionMetadata = timbuctooActions.getCollectionMetadata(DEFAULT_COLLECTION_NAME);
    DataStream<ReadEntity> result = timbuctooActions.getCollection(collectionMetadata, 0, 10, false,
      (entity, entityVertex) -> {
      }, (traversalSource, vre, target, relationRef) -> {
      }
    );

    assertThat(result.map(readEntity -> readEntity), hasSize(2));
  }

  @Test
  public void importTripleShouldAddANewRelationTypeForARelationWithANewType() {
    final ExtendedIterator<Triple> abadanInIran = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE);
    final ExtendedIterator<Triple> iranInAsia = createTripleIterator(IRAN_IS_PART_OF_ASIA_TRIPLE);

    instance.importTriple(true, abadanInIran.next());
    instance.importTriple(true, iranInAsia.next());

    List<RelationType> relationTypes = timbuctooActions.getRelationTypes();

    assertThat(relationTypes, hasSize(1));
  }

  @Test
  public void importTripleShouldAddTheSystemPropertiesToANewRelation() throws Exception {
    final String tripleString = ABADAN_IS_PART_OF_IRAN_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(true, tripleExtendedIterator.next());

    Optional<ReadEntity> readEntity = getReadEntity(DEFAULT_COLLECTION_NAME, ABADAN_URI);
    assertThat(readEntity.get().getRelations().get(0), allOf(
      hasProperty("relationRev", equalTo(1)),
      hasProperty("relationAccepted", equalTo(true)),
      hasProperty("relationRdfUri", equalTo(IS_PART_OF_URI)),
      hasProperty("relationId", notNullValue())
    ));
  }

  @Test
  public void importTripleShouldAddTheSystemPropertiesToANewEntity() throws Exception {
    final String tripleString = ABADAN_IS_PART_OF_IRAN_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(true, tripleExtendedIterator.next());

    Optional<ReadEntity> entityOpt = getReadEntity(DEFAULT_COLLECTION_NAME, ABADAN_URI);
    assertThat(entityOpt.get(), allOf(
      hasProperty("rev", equalTo(1)),
      hasProperty("deleted", equalTo(false)),
      hasProperty("id", notNullValue()),
      hasProperty("created", notNullValue()),
      hasProperty("modified", notNullValue())
    ));
  }

  @Test
  public void importTripleShouldConnectTheSubjectEntityToTheCollectionNamedByTheObject() throws Exception {
    final Triple abadan = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();

    instance.importTriple(true, abadan);

    Optional<ReadEntity> readEntity = getReadEntity(COLLECTION_NAME, ABADAN_URI);
    assertThat(readEntity, is(present()));
  }

  @Test
  public void importTripleShouldConnectTheSubjectToMultipleCollectionsNamedByTheObject() throws Exception {
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanIsAFictionalFeature = createTripleIterator(ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE).next();

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, abadanIsAFictionalFeature);

    assertThat(getReadEntity(COLLECTION_NAME, ABADAN_URI), is(present()));
    assertThat(getReadEntity(FICTIONAL_COLLECTION_NAME, ABADAN_URI), is(present()));
  }

  @Test
  public void importTripleMakesTheEntityAvailableAsConcept() throws Exception {
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();

    instance.importTriple(true, abadanIsAFeature);

    assertThat(getReadEntity(CONCEPTS_COLLECTION, ABADAN_URI), is(present()));
  }

  @Test
  public void importTripleAddsTheSubjectAndItsEntitiesToAKnownArchetype() throws Exception {
    Triple abadanIsAFeature = createSingleTriple(ABADAN_HAS_TYPE_FEATURE_TRIPLE);
    Triple featureIsLocation = createSingleTriple(FEATURE_SUBCLASS_OF_LOCATION_TRIPLE);

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, featureIsLocation);

    // FIXME: Make entity available as its new archetype
    assertThat(getReadEntity(LOCATION_COLLECTION, ABADAN_URI), is(present()));
    assertThat(getReadEntity(COLLECTION_NAME, ABADAN_URI), is(present()));
  }

  @Test
  public void importTripleRemovesTheSubjectAndItsEntitiesFromTheDefaultArchetype() throws Exception {
    Triple abadanIsAFeature = createSingleTriple(ABADAN_HAS_TYPE_FEATURE_TRIPLE);
    Triple featureIsLocation = createSingleTriple(FEATURE_SUBCLASS_OF_LOCATION_TRIPLE);

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, featureIsLocation);

    assertThat(getReadEntity(CONCEPTS_COLLECTION, ABADAN_URI), is(not(present())));
  }

  @Test
  public void importTripleAddsTheCollectionsAndTheArchetypeToTheTypesPropOfTheSubject() throws Exception {
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanIsAFictionalFeature = createTripleIterator(ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE).next();

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, abadanIsAFictionalFeature);

    Optional<ReadEntity> readEntity = getReadEntity(COLLECTION_NAME, ABADAN_URI);
    assertThat(readEntity.get(), hasProperty("types", containsInAnyOrder(TYPE_NAME, FICTIONAL_TYPE_NAME, "concept")));
  }

  @Test
  public void importTripleShouldSetThePropertiesForAllCollections() throws Exception {
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanIsAFictionalFeature = createTripleIterator(ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE).next();
    final Triple abadanHasPoint = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, abadanIsAFictionalFeature);
    instance.importTriple(true, abadanHasPoint);

    Optional<ReadEntity> type = getReadEntity(COLLECTION_NAME, ABADAN_URI);
    Optional<ReadEntity> fictionalType = getReadEntity(FICTIONAL_COLLECTION_NAME, ABADAN_URI);

    assertThat(type.get().getProperties(), hasItem(allOf(
      hasProperty("name", equalTo("point")),
      hasProperty("value", equalTo("30.35 48.28333333333333"))
    )));
    assertThat(fictionalType.get().getProperties(), hasItem(allOf(
      hasProperty("name", equalTo("point")),
      hasProperty("value", equalTo("30.35 48.28333333333333"))
    )));
  }

  @Test
  public void importTripleShouldSetsTheRdfUriAsDisplayName() throws Exception {
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanIsAFictionalFeature = createTripleIterator(ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE).next();
    final Triple abadanHasPoint = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(true, abadanIsAFeature);
    instance.importTriple(true, abadanIsAFictionalFeature);
    instance.importTriple(true, abadanHasPoint);

    Optional<ReadEntity> type = getReadEntity(COLLECTION_NAME, ABADAN_URI);
    Optional<ReadEntity> fictionalType = getReadEntity(FICTIONAL_COLLECTION_NAME, ABADAN_URI);
    assertThat(type.get().getDisplayName(), is(ABADAN_URI));
    assertThat(fictionalType.get().getDisplayName(), is(ABADAN_URI));
  }

  //FIXME: adding 2 collections to an entity will not copy the properties to the last collection (because collections
  // are

}
