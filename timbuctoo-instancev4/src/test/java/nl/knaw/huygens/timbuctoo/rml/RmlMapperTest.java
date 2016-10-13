package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfLiteral;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.PredicateObjectMapBuilder;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.ReferencingObjectMapBuilder;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.SubjectMapBuilder;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.TermMapBuilder;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.TriplesMapBuilder;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.rml.TripleMatcher.likeTriple;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument.rmlMappingDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RmlMapperTest {

  public static final String EXAMPLE_CLASS = "http://example.org/someClass";

  private static Node_URI uri(String uri) {
    return (Node_URI) NodeFactory.createURI(uri);
  }

  private static Node literal(String value) {
    return NodeFactory.createLiteral(value);
  }

  @Test
  public void generatesRdfTypeTriplesForSubjectMapsWithRrClass() {
    DataSource input = new TestDataSource(Lists.newArrayList(Maps.newHashMap()));

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/mapping1", trip -> trip
        .withSubjectMap(sm -> sm
          .withClass(rdf(EXAMPLE_CLASS))
          .withTermMap(tm -> tm
              .withConstantTerm("http://example.com/myItem")
          )
        )
      )
      .build(x -> Optional.of(input))
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(likeTriple(
      Node.ANY,
      uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
      uri(EXAMPLE_CLASS)
    )));
  }

  @Test
  public void generatesSubjectsForEachInputObject() {

    DataSource input = new TestDataSource(Lists.newArrayList(
      ImmutableMap.of("rdfUri", "http://www.example.org/example/1"),
      ImmutableMap.of("rdfUri", "http://www.example.org/example/2")
    ));

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/mapping1", trip -> trip
        .withSubjectMap(sm -> sm
          .withTermMap(tm -> tm
            .withColumnTerm("rdfUri")
          )
          .withClass(rdf(EXAMPLE_CLASS))
        )
      )
      .build(x -> Optional.of(input))
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(
      likeTriple(uri("http://www.example.org/example/1"), Node.ANY, Node.ANY),
      likeTriple(uri("http://www.example.org/example/2"), Node.ANY, Node.ANY)
    ));
  }

  @Test
  public void generatesPredicateForEachInputObject() {
    final String theNamePredicate = "http://example.org/vocab#name";

    DataSource input = new TestDataSource(Lists.newArrayList(ImmutableMap.of(
      "rdfUri", "http://www.example.org/example/1",
      "naam", "Bill"
    )));

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/mapping1", trip -> trip
        .withSubjectMap(sm -> sm
          .withTermMap(tm -> tm.withColumnTerm("rdfUri"))
        )
        .withPredicateObjectMap(pom -> pom
          .withPredicate(theNamePredicate)
          .withObjectMap(tm -> tm.withColumnTerm("naam"))
        )
      )
      .build(x -> Optional.of(input))
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(
      likeTriple(Node.ANY, uri(theNamePredicate), literal("Bill")))
    );
  }

  @Test
  public void handlesTemplateMaps() {
    DataSource input = new TestDataSource(Lists.newArrayList(ImmutableMap.of(
      "naam", "Bill"
    )));

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/mapping1", trip -> trip
        .withSubjectMap(sm -> sm
          .withClass(rdf(EXAMPLE_CLASS))
          .withTermMap(tm -> tm.withTemplateTerm("http://example.org/items/{naam}?blah"))
        )
      )
      .build(x -> Optional.of(input))
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(
      likeTriple(uri("http://example.org/items/Bill?blah"), Node.ANY, Node.ANY)
    ));
  }

  @Test
  public void canGenerateLinks() {
    final String theNamePredicate = "http://example.org/vocab#name";
    final String theWrittenByPredicate = "http://example.org/vocab#writtenBy";

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/personsMap", makePersonMap(theNamePredicate))
      .withTripleMap("http://example.org/documentsMap", makeDocumentMap(theWrittenByPredicate))
      .build(makePersonDocumentSourceFactory())
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(
      likeTriple(
        uri("http://www.example.org/persons/1"),
        uri(theNamePredicate),
        literal("Bill")
      ),
      likeTriple(
        uri("http://www.example.org/documents/1"),
        uri(theWrittenByPredicate),
        uri("http://www.example.org/persons/1")
      )
    ));
  }

  @Test
  public void canHandleMappingsInTheWrongOrder() {
    final String theNamePredicate = "http://example.org/vocab#name";
    final String theWrittenByPredicate = "http://example.org/vocab#writtenBy";

    List<Triple> result = rmlMappingDocument()
      .withTripleMap("http://example.org/documentsMap", makeDocumentMap(theWrittenByPredicate))
      .withTripleMap("http://example.org/personsMap", makePersonMap(theNamePredicate))
      .build(makePersonDocumentSourceFactory())
      .execute(new ThrowingErrorHandler())
      .collect(toList());

    assertThat(result, contains(
      likeTriple(
        uri("http://www.example.org/persons/1"),
        uri(theNamePredicate),
        literal("Bill")
      ),
      likeTriple(
        uri("http://www.example.org/documents/1"),
        uri(theWrittenByPredicate),
        uri("http://www.example.org/persons/1")
      )
    ));
  }

  @Test
  public void whenALinkToAnOtherObjectIsNotAvailableTheExceptionIsRegistered() {
    ErrorHandler errorHandler = mock(ErrorHandler.class);

    final String theNamePredicate = "http://example.org/vocab#name";
    final String theWrittenByPredicate = "http://example.org/vocab#writtenBy";
    final Map<String, Object> firstDocument = ImmutableMap.of(
      "rdfUri", "http://www.example.org/documents/1",
      "geschrevenDoor", "Bill"
    );

    rmlMappingDocument()
      .withTripleMap("http://example.org/personsMap", makePersonMap(theNamePredicate))
      .withTripleMap("http://example.org/documentsMap", makeDocumentMap(theWrittenByPredicate))
      .build(logicalSource -> {
        if (logicalSource.asIri().get().equals("http://example.org/persons")) {
          return Optional.of(new TestDataSource(Lists.newArrayList(ImmutableMap.of()), errorHandler));
        }
        if (logicalSource.asIri().get().equals("http://example.org/documents")) {
          return Optional.of(new TestDataSource(Lists.newArrayList(firstDocument), errorHandler));
        }
        return null;
      })
      .execute(new LoggingErrorHandler())
      .collect(toList());

    verify(errorHandler).linkError(firstDocument, "geschrevenDoor", "http://example.org/personsMap", "naam");
  }

  @Test(expected = RuntimeException.class)
  public void whenAMapperWithoutJoiningSupportIsReferencedByAnotherMapperTheExceptionIsRegistered() {
    final DataSource dataSource = mock(DataSource.class);
    final String theNamePredicate = "http://example.org/vocab#name";
    final String theWrittenByPredicate = "http://example.org/vocab#writtenBy";

    final RmlMappingDocument rmlMappingDocument = rmlMappingDocument()
      .withTripleMap("http://example.org/personsMap", makePersonMap(theNamePredicate))
      .withTripleMap("http://example.org/documentsMap", makeDocumentMap(theWrittenByPredicate))
      .build(logicalSource -> Optional.of(dataSource));

    assertThat(rmlMappingDocument.getErrors().size(), equalTo(1));

    assertThat(rmlMappingDocument.getErrors().get(0),
      equalTo("Datasource of triplesMap identified by http://example.org/personsMap, " +
        "requested by http://example.org/documentsMap does not support joining"));

    rmlMappingDocument.execute(new LoggingErrorHandler()).collect(toList());

  }


  private Consumer<TriplesMapBuilder> makeDocumentMap(String theWrittenByPredicate) {
    return trip -> trip
      .withLogicalSource(rdf("http://example.org/documents"))
      .withSubjectMap(sm -> sm
        .withTermMap(tm -> tm.withColumnTerm("rdfUri"))
      )
      .withPredicateObjectMap(pom -> pom
        .withPredicate(theWrittenByPredicate)
        .withReference(rm -> rm
          .withParentTriplesMap("http://example.org/personsMap")
          .withJoinCondition("geschrevenDoor", "naam")
        )
      );
  }

  private Consumer<TriplesMapBuilder>  makePersonMap(String theNamePredicate) {
    return trip -> trip
      .withLogicalSource(rdf("http://example.org/persons"))
      .withSubjectMap(sm -> sm
        .withTermMap(tm -> tm.withColumnTerm("rdfUri"))
      )
      .withPredicateObjectMap(pom -> pom
        .withPredicate(theNamePredicate)
        .withObjectMap(tm -> tm.withColumnTerm("naam"))
      );
  }

  private Function<RdfResource, Optional<DataSource>> makePersonDocumentSourceFactory() {
    return logicalSource -> {
      if (logicalSource.asIri().get().equals("http://example.org/persons")) {
        return Optional.of(new TestDataSource(Lists.newArrayList(ImmutableMap.of(
          "rdfUri", "http://www.example.org/persons/1",
          "naam", "Bill"
        ))));
      }
      if (logicalSource.asIri().get().equals("http://example.org/documents")) {
        return Optional.of(new TestDataSource(Lists.newArrayList(ImmutableMap.of(
          "rdfUri", "http://www.example.org/documents/1",
          "geschrevenDoor", "Bill"
        ))));
      }
      return null;
    };
  }

  public RdfResource rdf(String uri) {
    return new MockRdfResource(uri, new HashMap<>());
  }

  private class MockRdfResource implements RdfResource {

    private final String uri;
    private final Map<String, Set<RdfResource>> outs;

    MockRdfResource(String uri, Map<String, Set<RdfResource>> outs) {
      this.uri = uri;
      this.outs = outs;
    }

    @Override
    public Set<RdfResource> out(String predicateUri) {
      return outs.putIfAbsent(predicateUri, new HashSet<>());
    }

    @Override
    public Optional<String> asIri() {
      return Optional.of(uri);
    }

    @Override
    public Optional<RdfLiteral> asLiteral() {
      return Optional.empty();
    }
  }
}
