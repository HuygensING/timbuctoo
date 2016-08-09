package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.UriSource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.junit.Test;

import java.util.Map;

import static nl.knaw.huygens.timbuctoo.rml.TripleMatcher.likeTriple;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlLogicalSource.rrLogicalSource;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument.rmlMappingDocument;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap.rrPredicateObjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrSubjectMap.rrSubjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap.rrTriplesMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrRefObjectMap.rrRefObjectMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RmlMapperTest {

  public static final Node_URI EXAMPLE_CLASS = uri("http://example.org/someClass");

  private static Node_URI uri(String uri) {
    return (Node_URI) NodeFactory.createURI(uri);
  }

  private static Node literal(String value) {
    return NodeFactory.createLiteral(value);
  }

  @Test
  public void generatesRdfTypeTriplesForSubjectMapsWithRrClass() {
    DataSource input = new TestDataSource(Lists.newArrayList(Maps.newHashMap()));

    TripleConsumer consumer = mock(TripleConsumer.class);

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/mapping1"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/mapping")
        )
        .withSubjectMap(rrSubjectMap()
          .withConstantTerm(uri("http://example.com/myItem"))
          .withClass(EXAMPLE_CLASS)
        )
      )
      .build(x -> input)
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(likeTriple(
      Node.ANY,
      uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
      EXAMPLE_CLASS
    )));
  }

  @Test
  public void generatesSubjectsForEachInputObject() {
    TripleConsumer consumer = mock(TripleConsumer.class);
    Map<String, Object> entity1 = Maps.newHashMap();
    entity1.put("rdfUri", "http://www.example.org/example/1");
    Map<String, Object> entity2 = Maps.newHashMap();
    entity2.put("rdfUri", "http://www.example.org/example/2");
    DataSource input = new TestDataSource(Lists.newArrayList(entity1, entity2));

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/mapping1"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/mapping")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
          .withClass(EXAMPLE_CLASS)
        )
      )
      .build(x -> input)
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(likeTriple(uri("http://www.example.org/example/1"), Node.ANY, Node.ANY)));
    verify(consumer).accept(argThat(likeTriple(uri("http://www.example.org/example/2"), Node.ANY, Node.ANY)));
    verifyNoMoreInteractions(consumer);
  }

  @Test
  public void generatesPredicateForEachInputObject() {
    final Node_URI theNamePredicate = uri("http://example.org/vocab#name");

    TripleConsumer consumer = mock(TripleConsumer.class);
    DataSource input = new TestDataSource(Lists.newArrayList(ImmutableMap.of(
      "rdfUri", "http://www.example.org/example/1",
      "naam", "Bill"
    )));

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/mapping1"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/mapping")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
          .withClass(EXAMPLE_CLASS)
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theNamePredicate)
          .withColumn("naam")
        )
      )
      .build(x -> input)
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(likeTriple(Node.ANY, theNamePredicate, literal("Bill"))));
  }


  @Test
  public void handlesTemplateMaps() {
    TripleConsumer consumer = mock(TripleConsumer.class);
    DataSource input = new TestDataSource(Lists.newArrayList(ImmutableMap.of(
      "naam", "Bill"
    )));

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/mapping1"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/mapping")
        )
        .withSubjectMap(rrSubjectMap()
          .withTemplateTerm("http://example.org/items/{naam}?blah")
          .withClass(EXAMPLE_CLASS)
        )
      )
      .build(x -> input)
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(
      likeTriple(uri("http://example.org/items/Bill?blah"), Node.ANY, Node.ANY)
    ));
  }

  @Test
  public void canGenerateLinks() {
    final Node_URI theNamePredicate = uri("http://example.org/vocab#name");
    final Node_URI theWrittenByPredicate = uri("http://example.org/vocab#writtenBy");

    TripleConsumer consumer = mock(TripleConsumer.class);

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/personsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/persons")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theNamePredicate)
          .withColumn("naam")
        )
      )
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/documentsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/documents")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theWrittenByPredicate)
          .withReference(rrRefObjectMap()
            .withParentTriplesMap("http://example.org/personsMap")
            .withJoinCondition("geschrevenDoor", "naam")
          )
        )
      )
      .build(logicalSource -> {
        if (logicalSource.getSource() instanceof UriSource) {
          UriSource source = (UriSource) logicalSource.getSource();
          if (source.getUri().equals("http://example.org/persons")) {
            return new TestDataSource(Lists.newArrayList(ImmutableMap.of(
              "rdfUri", "http://www.example.org/persons/1",
              "naam", "Bill"
            )));
          }
          if (source.getUri().equals("http://example.org/documents")) {
            return new TestDataSource(Lists.newArrayList(ImmutableMap.of(
              "rdfUri", "http://www.example.org/documents/1",
              "geschrevenDoor", "Bill"
            )));
          }
        }
        return null;
      })
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(likeTriple(
      uri("http://www.example.org/persons/1"),
      theNamePredicate,
      literal("Bill")
    )));
    verify(consumer).accept(argThat(likeTriple(
      uri("http://www.example.org/documents/1"),
      theWrittenByPredicate,
      uri("http://www.example.org/persons/1")
    )));
  }

  @Test
  public void canHandleMappingsInTheWrongOrder() {
    final Node_URI theNamePredicate = uri("http://example.org/vocab#name");
    final Node_URI theWrittenByPredicate = uri("http://example.org/vocab#writtenBy");

    TripleConsumer consumer = mock(TripleConsumer.class);

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/documentsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/documents")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theWrittenByPredicate)
          .withReference(rrRefObjectMap()
            .withParentTriplesMap("http://example.org/personsMap")
            .withJoinCondition("geschrevenDoor", "naam")
          )
        )
      )
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/personsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/persons")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theNamePredicate)
          .withColumn("naam")
        )
      )
      .build(logicalSource -> {
        if (logicalSource.getSource() instanceof UriSource) {
          UriSource source = (UriSource) logicalSource.getSource();
          if (source.getUri().equals("http://example.org/persons")) {
            return new TestDataSource(Lists.newArrayList(ImmutableMap.of(
              "rdfUri", "http://www.example.org/persons/1",
              "naam", "Bill"
            )));
          }
          if (source.getUri().equals("http://example.org/documents")) {
            return new TestDataSource(Lists.newArrayList(ImmutableMap.of(
              "rdfUri", "http://www.example.org/documents/1",
              "geschrevenDoor", "Bill"
            )));
          }
        }
        return null;
      })
      .execute()
      .forEach(consumer);

    verify(consumer).accept(argThat(likeTriple(
      uri("http://www.example.org/persons/1"),
      theNamePredicate,
      literal("Bill")
    )));
    verify(consumer).accept(argThat(likeTriple(
      uri("http://www.example.org/documents/1"),
      theWrittenByPredicate,
      uri("http://www.example.org/persons/1")
    )));
  }

  @Test
  public void whenALinkToAnOtherObjectIsNotAvailableTheExceptionIsRegistered() {
    ErrorHandler errorHandler = mock(ErrorHandler.class);
    final Node_URI theNamePredicate = uri("http://example.org/vocab#name");
    final Node_URI theWrittenByPredicate = uri("http://example.org/vocab#writtenBy");

    Map<String, Object> documentMap = ImmutableMap.of(
      "rdfUri", "http://www.example.org/documents/1",
      "geschrevenDoor", "Bill"
    );

    TripleConsumer consumer = mock(TripleConsumer.class);

    rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/personsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/persons")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theNamePredicate)
          .withColumn("naam")
        )
      )
      .withTripleMap(rrTriplesMap()
        .withUri(uri("http://example.org/documentsMap"))
        .withLogicalSource(rrLogicalSource()
          .withSource("http://example.org/documents")
        )
        .withSubjectMap(rrSubjectMap()
          .withColumnTerm("rdfUri")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate(theWrittenByPredicate)
          .withReference(rrRefObjectMap()
            .withParentTriplesMap("http://example.org/personsMap")
            .withJoinCondition("geschrevenDoor", "naam")
          )
        )
      )
      .build(logicalSource -> {
        if (logicalSource.getSource() instanceof UriSource) {
          UriSource source = (UriSource) logicalSource.getSource();
          if (source.getUri().equals("http://example.org/persons")) {
            return new TestDataSource(Lists.newArrayList(Maps.newHashMap()), errorHandler);
          }
          if (source.getUri().equals("http://example.org/documents")) {
            return new TestDataSource(Lists.newArrayList(documentMap), errorHandler);
          }
        }
        throw new RuntimeException("Unexpected data source request");
      })
      .execute()
      .forEach(consumer);

    verify(errorHandler).handleLink(documentMap, "geschrevenDoor", "http://example.org/personsMap", "naam");
  }

  //mapping schrijven voor 2 sheets van EuropeseMigratie (met een relatie) (in java)
  //DataSource schrijven voor de geimporteerde excel vertices
  //rest endpoint die die mapping uitvoert op de database met de geimporteerde data
}
