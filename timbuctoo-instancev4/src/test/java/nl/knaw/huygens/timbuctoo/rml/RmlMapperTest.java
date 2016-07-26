package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RmlMapperTest {

  public static final String EXAMPLE_CLASS = "http://example.org/someClass";

  @Test
  public void generatesRdfTypeTriplesForSubjectMapsWithRrClass() {
    RmlMappingDocument map = new RmlMappingDocument(
      new TriplesMap(
        new SubjectMap(new RmlConstant("http://example.com/myItem"), EXAMPLE_CLASS)
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    Function<LogicalSource, Iterable<Map<String, Object>>> input =
      logicalSource -> Lists.newArrayList(Maps.newHashMap());

    RmlMapper.execute(input, map, consumer);

    ArgumentCaptor<Triple> tripleArgumentCaptor = ArgumentCaptor.forClass(Triple.class);
    verify(consumer).accept(tripleArgumentCaptor.capture());
    assertThat(
      tripleArgumentCaptor.getValue().getPredicate().getURI(),
      is("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    );
    assertThat(
      tripleArgumentCaptor.getValue().getObject().getURI(),
      is(EXAMPLE_CLASS)
    );
  }

  @Test
  public void generatesSubjectsForEachInputObject() {
    RmlMappingDocument map = new RmlMappingDocument(
      new TriplesMap(
        new SubjectMap(new RmlReference("rdfUri"), EXAMPLE_CLASS)
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    Function<LogicalSource, Iterable<Map<String, Object>>> input =
      logicalSource -> {
        Map<String, Object> entity1 = Maps.newHashMap();
        entity1.put("rdfUri", "http://www.example.org/example/1");
        Map<String, Object> entity2 = Maps.newHashMap();
        entity2.put("rdfUri", "http://www.example.org/example/2");
        return Lists.newArrayList(entity1, entity2);
      };

    RmlMapper.execute(input, map, consumer);

    ArgumentCaptor<Triple> tripleArgumentCaptor = ArgumentCaptor.forClass(Triple.class);
    verify(consumer, times(2)).accept(tripleArgumentCaptor.capture());
    List<Triple> allValues = tripleArgumentCaptor.getAllValues();
    assertThat(allValues, hasSize(2));
    assertThat(allValues.get(0).getSubject().getURI(), is("http://www.example.org/example/1"));
    assertThat(allValues.get(1).getSubject().getURI(), is("http://www.example.org/example/2"));
  }

  @Test
  public void generatessSubjectsForEachInputObject() {
    final String theNamePredicate = "http://example.org/vocab#name";

    RmlMappingDocument map = new RmlMappingDocument(
      new TriplesMap(
        new SubjectMap(new RmlReference("rdfUri")),
        new PredicateObjectMap(theNamePredicate, new RmlReference("naam"))
      )
    );
    TripleConsumer consumer = mock(TripleConsumer.class);
    Function<LogicalSource, Iterable<Map<String, Object>>> input =
      logicalSource -> Lists.newArrayList(ImmutableMap.of(
        "rdfUri", "http://www.example.org/example/1",
        "naam", "Bill"
      ));

    RmlMapper.execute(input, map, consumer);

    ArgumentCaptor<Triple> tripleArgumentCaptor = ArgumentCaptor.forClass(Triple.class);
    verify(consumer).accept(tripleArgumentCaptor.capture());
    assertThat(tripleArgumentCaptor.getValue().getPredicate().getURI(), is(theNamePredicate));
    assertThat(tripleArgumentCaptor.getValue().getObject().getLiteral().getValue(), is("Bill"));
  }
}
