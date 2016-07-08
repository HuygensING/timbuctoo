package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.knaw.huygens.timbuctoo.rdf.Importer.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ImporterTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";

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

  @Test
  public void importTripleShouldCreateAVertexFromATriple() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(ABADAN_POINT_TRIPLE);

    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).hasNext(), is(true));
  }

  @Test
  public void importTripleShouldReuseTheExistingNodeWithUriFromSubject() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    final String tripleString = ABADAN_POINT_TRIPLE + "\n" + ABADAN_LAT_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());
    instance.importTriple(tripleExtendedIterator.next());


    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).count().next(),
      equalTo(1L));
  }

  @Test
  public void importTripleShouldMapATripleDescribingAPropertyToAVertexProperty() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    final String tripleString = ABADAN_POINT_TRIPLE + "\n" + ABADAN_LAT_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());
    instance.importTriple(tripleExtendedIterator.next());


    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ABADAN_URI).next(), likeVertex()
      .withProperty("http://www.georss.org/georss/point", "30.35 48.28333333333333")
      .withProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat", "30.35")
    );
  }


  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndANewObjectVertex() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    final String tripleString = ABADAN_IS_PART_OF_IRAN_TRIPLE;
    final ExtendedIterator<Triple> tripleExtendedIterator = createTripleIterator(tripleString);

    instance.importTriple(tripleExtendedIterator.next());

    assertThat(graphWrapper.getGraph().traversal().V().both(IS_PART_OF_URI).toList(), containsInAnyOrder(
      likeVertex().withProperty("rdfUri", ABADAN_URI),
      likeVertex().withProperty("rdfUri", IRAN_URI)
    ));
  }

  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndAnExistingObjectVertex() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    final Triple abadan = createTripleIterator(ABADAN_POINT_TRIPLE).next();
    final Triple iran = createTripleIterator(IRAN_POINT_TRIPLE).next();
    final Triple relation = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    instance.importTriple(abadan);
    instance.importTriple(iran);
    instance.importTriple(relation);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, P.within(ABADAN_URI, IRAN_URI)).count().next(),
      is(2L));
  }

  @Test
  public void importTripleShouldConnectResultingSubjectEntityToTheUnknownCollection() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper collectionMapper = mock(CollectionMapper.class);
    Importer instance = new Importer(graphWrapper, collectionMapper);
    final Triple abadan = createTripleIterator(ABADAN_POINT_TRIPLE).next();

    instance.importTriple(abadan);

    verify(collectionMapper).addToCollection(
      argThat(likeVertex().withProperty(RDF_URI_PROP, ABADAN_URI)),
      argThat(is("unknown")));
  }

  @Test
  public void importTripleShouldConnectResultingObjectEntityToACollection() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper collectionMapper = mock(CollectionMapper.class);
    Importer instance = new Importer(graphWrapper, collectionMapper);
    final Triple abadan = createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    instance.importTriple(abadan);

    verify(collectionMapper).addToCollection(
      argThat(likeVertex().withProperty(RDF_URI_PROP, IRAN_URI)),
      argThat(is("unknown")));
  }

  // given a new entity resulting from the subject or object of a triple
  // - its vertex must be part of a collection
  // - if the triple predicate is rdf:type:
  //   - it must become part of the collection named by triple.getObject().getUri()
  //   - it must not be part of the "unknown" collection anymore
  // - else:
  //   - it must be(come) part of the "unknown" collection,


  private ExtendedIterator<Triple> createTripleIterator(String tripleString) {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model1.read(in, null, "N3");
    return model1.getGraph().find(Triple.ANY);
  }


// given a triple describing an rdf:type relation
// - the subject vertex must become part of the collection name from triple.getObject()

// given a triple
//

// import should connect the subject of a triple to a collection
//
// import should create a collection from the subject if it does describe a colleciton.

// Container node should be reused.
// relation to container node should only be created if there is not one already


/*  @Test
  public void importTripleShouldConnectTheSubjectToAContainerNode() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
    Model model = ModelFactory.createDefaultModel();
    final String tripleString = "<http://tl.dbpedia.org/resource/Abadan,_Iran> " +
      "<http://www.georss.org/georss/point> " +
      "\"30.35 48.28333333333333\"@tl .";
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model.read(in, null, "N3");
    final ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find(Triple.ANY);

    instance.importTriple(tripleExtendedIterator.next());
    final GraphTraversal<Vertex, Vertex> actualEntityNode = graphWrapper.getGraph().traversal().V()
                                                                        .in(Collection.HAS_ENTITY_RELATION_NAME);


    assertThat(actualEntityNode.hasNext(), equalTo(true));
    assertThat(actualEntityNode.next(),
      likeVertex().withLabel(Collection.COLLECTION_ENTITIES_LABEL));
  }*/






































/*
- constructor should create a new VRE vertex based on the datasetIdentifier
- constructor should clear an existing VRE vertex for the datasetIdentifier of any configuration data / collection
vertices and entity nodes
- constructor should add the "unknown" collection with hasArchetype -> "things" and hasEntityNode -> (entityNode)

- import should treat the triple as:
 - a setArchetype instruction when triple.getPredicate() returns the URI rdfs:SubClassOf
 - a setCollection instruction when the triple.getPredicate() returns the URI rfd:type
 - a setRelation instruction when triple.getObject() returns a URI
 - a setProperty instruction when triple.getObject() does not return a URI (but a literal)

- setArchetype(triple) should
 - findOrCreate a vertex for the collection identified by triple.getSubject() (collectionVertex)
 **findOrCreateCollection**
 - find the vertex of the archetype by the URI from triple.getObject()  (archetypeVertex)
 - if archetype found:
   - replace hasArchetype relation of collectionVertex with hasArchetype pointing to the archetypeVertex

- setCollection(triple) should
 - findOrCreate a(n) (entity) vertex for the URI from triple.getSubject() (entityVertex)
 - findOrCreate a (collection) vertex  for the URI from triple.getObject() (collectionVertex) **findOrCreateCollection**

 - if entityVertex has relation to entityNode of "unknown" collection:
   - remove edge "hasEntity" (inverse) from entityVertex
 - create inverse "hasEntity edge to the entityNode of the collection

- setRelation(triple) should
 - findOrCreate a(n) (entity) vertex for the URI from triple.getSubject() (sourceVertex)
 - findOrCreate a(n) (entity) vertex for the URI from triple.getObject() (targetVertex)
 - get uri from triple.getPredicate() (relationURI)
 - findOrCreateRelationType(relationURI, sourceVertex, targetVertex) (relationDesc)
 - if relationDesc = inverse:
  - create edge labeled relationDesc.edgeLabel from sourceVertex to targetVertex
 - else:
  - create edge labeled relationDesc.edgeLabel from targetVertex to sourceVertex


- findOrCreate(uri) should
 - detect vertex by uri (entityVertex)
 - if not detected: create
 - if entityVertex does not have inverse hasEntity relation: create to "unknown" collection
 - if entityVertex does not have rdfUri prop: set prop

- findOrCreateCollection(uri) should
 - detect vertex for by uri (collectionVertex)
 - if not detected: create
 - if collectionVertex does not have relation to VRE: create
 - if collectionVertex does not have relation to archetype: create for "things"
 - if collectionVertex does not have an entityNode: create
 - if collectionVertex does not have rdfUri prop: set prop

- findOrCreateRelationType(relationURI, sourceVertex, targetVertex)
 - find relationType entry by rdfUri prop == relationUri
 - if exists return relationDescription
 - else ....
 */
}
