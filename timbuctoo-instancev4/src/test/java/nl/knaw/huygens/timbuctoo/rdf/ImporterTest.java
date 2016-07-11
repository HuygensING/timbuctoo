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
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ImporterTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";
  private static final String IS_PART_OF_NAME = "isPartOf";
  private static final String TYPE_URI = "http://www.opengis.net/gml/_Feature";
  private static final String TYPE_NAME = "_Feature";

  private static final String ABADAN_HAS_TYPE_FEATURE_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
      "<" + TYPE_URI + "> .";

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
      .withProperty("point", "30.35 48.28333333333333")
      .withProperty("lat", "30.35")
    );
  }


  @Test
  public void importTripleShouldMapToARelationBetweenTheSubjectAndANewObjectVertex() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    Importer instance = new Importer(graphWrapper);
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

  @Test
  public void importTripleShouldConnectTheSubjectEntityToTheCollectionOfTheObject() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper collectionMapper = mock(CollectionMapper.class);
    Importer instance = new Importer(graphWrapper, collectionMapper);
    final Triple abadan = createTripleIterator(ABADAN_HAS_TYPE_FEATURE_TRIPLE).next();

    instance.importTriple(abadan);

    verify(collectionMapper).addToCollection(
      argThat(likeVertex().withProperty(RDF_URI_PROP, ABADAN_URI)),
      argThat(is(TYPE_NAME)));
    verifyNoMoreInteractions(collectionMapper);
  }

  private ExtendedIterator<Triple> createTripleIterator(String tripleString) {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model1.read(in, null, "N3");
    return model1.getGraph().find(Triple.ANY);
  }
}
