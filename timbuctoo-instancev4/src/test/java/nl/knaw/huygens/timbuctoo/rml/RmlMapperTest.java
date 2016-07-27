package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.rml.rmldata.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrLogicalSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrSubjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrColumn;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrConstant;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrJoinCondition;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.rml.TripleMatcher.likeTriple;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RmlMapperTest {

  public static final Node_URI EXAMPLE_CLASS = makeUriNode("http://example.org/someClass");

  private static Node_URI makeUriNode(String uri) {
    return (Node_URI) NodeFactory.createURI(uri);
  }

  private static Node makeLiteralNode(String value) {
    return NodeFactory.createLiteral(value);
  }

  @Test
  public void generatesRdfTypeTriplesForSubjectMapsWithRrClass() {
    RmlMappingDocument map = new RmlMappingDocument(
      new RrTriplesMap(
        new RrLogicalSource(makeUriNode("http://example.org/mapping"), null),
        new RrSubjectMap(new RrConstant(NodeFactory.createURI("http://example.com/myItem")), EXAMPLE_CLASS)
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    DataSource input = new TestDataSource(ImmutableMap.of(
      "http://example.org/mapping", Lists.newArrayList(Maps.newHashMap())
    ));


    RmlMapper.execute(input, map, consumer);

    verify(consumer).accept(argThat(likeTriple(
      Node.ANY,
      makeUriNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
      EXAMPLE_CLASS
    )));
  }

  @Test
  public void generatesSubjectsForEachInputObject() {
    RmlMappingDocument map = new RmlMappingDocument(
      new RrTriplesMap(
        new RrLogicalSource(makeUriNode("http://example.org/mapping"), null),
        new RrSubjectMap(new RrColumn("rdfUri"), EXAMPLE_CLASS)
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    Map<String, Object> entity1 = Maps.newHashMap();
    entity1.put("rdfUri", "http://www.example.org/example/1");
    Map<String, Object> entity2 = Maps.newHashMap();
    entity2.put("rdfUri", "http://www.example.org/example/2");
    DataSource input = new TestDataSource(ImmutableMap.of(
      "http://example.org/mapping", Lists.newArrayList(entity1, entity2)));

    RmlMapper.execute(input, map, consumer);

    verify(consumer).accept(argThat(likeTriple(makeUriNode("http://www.example.org/example/1"), Node.ANY, Node.ANY)));
    verify(consumer).accept(argThat(likeTriple(makeUriNode("http://www.example.org/example/2"), Node.ANY, Node.ANY)));
    verifyNoMoreInteractions(consumer);
  }

  @Test
  public void generatesPredicateForEachInputObject() {
    final Node_URI theNamePredicate = makeUriNode("http://example.org/vocab#name");

    RmlMappingDocument map = new RmlMappingDocument(
      new RrTriplesMap(
        new RrLogicalSource(makeUriNode("http://example.org/mapping"), null),
        new RrSubjectMap(new RrColumn("rdfUri")),
        new RrPredicateObjectMap(theNamePredicate, new RrColumn("naam"))
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    DataSource input = new TestDataSource(ImmutableMap.of(
      "http://example.org/mapping", Lists.newArrayList(ImmutableMap.of(
        "rdfUri", "http://www.example.org/example/1",
        "naam", "Bill"
      ))));

    RmlMapper.execute(input, map, consumer);

    verify(consumer).accept(argThat(likeTriple(Node.ANY, theNamePredicate, makeLiteralNode("Bill"))));
  }

  @Test
  public void canGenerateLinks() {
    final Node_URI theNamePredicate = makeUriNode("http://example.org/vocab#name");
    final Node_URI theWrittenByPredicate = makeUriNode("http://example.org/vocab#writtenBy");
    final RrTriplesMap mapping1 = new RrTriplesMap(
      new RrLogicalSource(makeUriNode("http://example.org/mapping1"), null),
      new RrSubjectMap(new RrColumn("rdfUri")),
      new RrPredicateObjectMap(theNamePredicate, new RrColumn("naam"))
    );
    final RrTriplesMap mapping2 = new RrTriplesMap(
      new RrLogicalSource(makeUriNode("http://example.org/mapping2"), null),
      new RrSubjectMap(new RrColumn("rdfUri")),
      new RrPredicateObjectMap(
        theWrittenByPredicate,
        new RrRefObjectMap(
          mapping1,
          new RrJoinCondition("geschrevenDoor", "naam")
        )
      )
    );
    RmlMappingDocument map = new RmlMappingDocument(mapping1, mapping2);
    HashMap<String, Object> person = new HashMap<>();
    person.put("rdfUri", "http://www.example.org/persons/1");
    person.put("naam", "Bill");
    HashMap<String, Object> document = new HashMap<>();
    document.put("rdfUri", "http://www.example.org/documents/1");
    document.put("geschrevenDoor", "Bill");
    DataSource input = new TestDataSource(ImmutableMap.of(
      "http://example.org/mapping1", Lists.newArrayList(person),
      "http://example.org/mapping2", Lists.newArrayList(document)
    ));
    TripleConsumer consumer = mock(TripleConsumer.class);

    RmlMapper.execute(input, map, consumer);

    verify(consumer).accept(argThat(likeTriple(
        makeUriNode("http://www.example.org/persons/1"),
        theNamePredicate,
        makeLiteralNode("Bill")
      )));
    verify(consumer).accept(argThat(likeTriple(
      makeUriNode("http://www.example.org/documents/1"),
      theWrittenByPredicate,
      makeUriNode("http://www.example.org/persons/1")
    )));
  }

  //mapping schrijven voor 2 sheets van EuropeseMigratie (met een relatie) (in java)
  //DataSource schrijven voor de geimporteerde excel vertices
  //rest endpoint die die mapping uitvoert op de database met de geimporteerde data
}
