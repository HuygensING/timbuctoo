package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.GraphUtil.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class TripleImporterTest {
  private static final String VRE_NAME = "vreName";
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";
  private static final String IS_PART_OF_NAME = "isPartOf";
  private static final String TYPE_URI = "http://www.opengis.net/gml/_Feature";
  private static final String TYPE_NAME = "_Feature";
  private static final String FICTIONAL_TYPE_URI = "http://www.opengis.net/gml/_FictionalFeature";
  private static final String FICTIONAL_TYPE_NAME = "_FictionalFeature";

  private static final String DEFAULT_ENTITY_TYPE_NAME = "unknown";

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
      "<" + IRAN_URI + ">";
  private GraphWrapper graphWrapper;


  @Before
  public void setUp() throws Exception {
    graphWrapper = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME);
      })
      .wrap();
  }

  @Test
  public void importTripleShouldCreateAVertexFromATriple() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(ABADAN_POINT_TRIPLE);

    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).hasNext(), is(true));
  }

  @Test
  public void importTripleShouldReuseTheExistingNodeWithUriFromSubject() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final String tripleString = ABADAN_POINT_TRIPLE + "\n" + ABADAN_LAT_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());
    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).count().next(),
      equalTo(1L));
  }

  @Test
  public void importTripleShouldMapATripleDescribingAPropertyToAVertexProperty() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final String tripleString = ABADAN_POINT_TRIPLE + "\n" + ABADAN_LAT_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());
    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).next(), likeVertex()
      .withProperty(VRE_NAME + DEFAULT_ENTITY_TYPE_NAME + "_" + "point", "30.35 48.28333333333333")
      .withProperty(VRE_NAME + DEFAULT_ENTITY_TYPE_NAME + "_" + "lat", "30.35")
    );
  }

  @Test
  public void importTripleShouldSetThePrefixBasedOnTheActualCollectionOfTheSubjectVertex() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadanHasTypeFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanPointTriple = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(abadanHasTypeFeature);
    instance.importTriple(abadanPointTriple);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).next(), likeVertex()
      .withoutProperty(VRE_NAME + DEFAULT_ENTITY_TYPE_NAME + "_" + "point")
    );

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).next(), likeVertex()
      .withProperty(VRE_NAME + TYPE_NAME + "_" + "point", "30.35 48.28333333333333")
    );
  }

  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndANewObjectVertex() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final String tripleString = ABADAN_IS_PART_OF_IRAN_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().both(IS_PART_OF_NAME).toList(), containsInAnyOrder(
      likeVertex().withProperty("rdfUri", ABADAN_URI),
      likeVertex().withProperty("rdfUri", IRAN_URI)
    ));
  }


  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndAnExistingObjectVertex() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadan = createTripleIterator(ABADAN_POINT_TRIPLE).next();
    final Triple iran = createTripleIterator(IRAN_POINT_TRIPLE).next();
    final Triple relation = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    instance.importTriple(abadan);
    instance.importTriple(iran);
    instance.importTriple(relation);

    assertThat(graphWrapper.getGraph().traversal().V().has(
      RDF_URI_PROP, P.within(ABADAN_URI, IRAN_URI)).count().next(),
      is(2L));
  }

  @Test
  public void importTripleShouldConnectResultingSubjectEntityToTheUnknownCollection() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadan = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(abadan);

    GraphTraversal<Vertex, Vertex> collectionVertex = graphWrapper.getGraph().traversal().V()
                                                                  .has(RDF_URI_PROP, ABADAN_URI)
                                                                  .in(Collection.HAS_ENTITY_RELATION_NAME)
                                                                  .in(Collection.HAS_ENTITY_NODE_RELATION_NAME);
    assertThat(collectionVertex.hasNext(), is(true));
    assertThat(collectionVertex.next(), likeVertex()
      .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "unknowns")
      .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, DEFAULT_ENTITY_TYPE_NAME));
  }

  @Test
  public void importTripleShouldConnectResultingObjectEntityToACollection() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadan = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    instance.importTriple(abadan);

    GraphTraversal<Vertex, Vertex> collectionVertex = graphWrapper.getGraph().traversal().V()
                                                                  .has(RDF_URI_PROP, IRAN_URI)
                                                                  .in(Collection.HAS_ENTITY_RELATION_NAME)
                                                                  .in(Collection.HAS_ENTITY_NODE_RELATION_NAME);
    assertThat(collectionVertex.hasNext(), is(true));
    assertThat(collectionVertex.next(), likeVertex()
      .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "unknowns")
      .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, DEFAULT_ENTITY_TYPE_NAME));
  }

  @Test
  public void importTripleShouldConnectTheSubjectEntityToTheCollectionNamedByTheObject() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadan = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();

    instance.importTriple(abadan);

    GraphTraversal<Vertex, Vertex> collectionVertex = graphWrapper.getGraph().traversal().V()
                                                                  .has(RDF_URI_PROP, ABADAN_URI)
                                                                  .in(Collection.HAS_ENTITY_RELATION_NAME)
                                                                  .in(Collection.HAS_ENTITY_NODE_RELATION_NAME);

    assertThat(collectionVertex.hasNext(), is(true));
    assertThat(collectionVertex.next(), likeVertex()
      .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, TYPE_NAME + "s")
      .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, TYPE_NAME));
  }

  @Test
  public void importTripleShouldConnectTheSubjectToMultipleCollectionsNamedByTheObject() {
    TripleImporter instance = new TripleImporter(graphWrapper, VRE_NAME);
    final Triple abadanIsAFeature = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();
    final Triple abadanIsAFictionalFeature = createTripleIterator(ABADAN_HAS_TYPE_FICTIONAL_FEATURE_TRIPLE).next();

    instance.importTriple(abadanIsAFeature);
    instance.importTriple(abadanIsAFictionalFeature);

    GraphTraversal<Vertex, Vertex> collectionVertices = graphWrapper.getGraph().traversal().V()
                                                                  .has(RDF_URI_PROP, ABADAN_URI)
                                                                  .in(Collection.HAS_ENTITY_RELATION_NAME)
                                                                  .in(Collection.HAS_ENTITY_NODE_RELATION_NAME);
    assertThat(collectionVertices.count().next(), is(2L));

  }

  private ExtendedIterator<Triple> createTripleIterator(String tripleString) {
    Model model = createModel(tripleString);
    return model.getGraph().find(Triple.ANY);
  }

  private Model createModel(String tripleString) {
    Model model = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model.read(in, null, "N3");
    return model;
  }
}
