package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.PredicateMatcher.predicate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

public class BdbSchemaStoreTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  public void generatesASchemaForAllAdded() throws Exception {
    final DummyDataStorage dataStore = new DummyDataStorage();

    ChangeFetcher changeFetcher = new DummyChangeFetcher(
      CursorQuad.create("subj", "pred", Direction.OUT, ChangeType.ASSERTED,
          "obj", null, null, null, ""),
      CursorQuad.create("subj", "langPred", Direction.OUT, ChangeType.ASSERTED,
          "value", RdfConstants.LANGSTRING, "en", null, ""),
      CursorQuad.create("subj", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
          "type", null, null, null, ""),
      CursorQuad.create("obj", "pred", Direction.IN, ChangeType.ASSERTED,
          "subj", null, null, null, "")
    );

    BdbSchemaStore schemaStore = new BdbSchemaStore(dataStore, new ImportStatus(new LogList()));

    schemaStore.start();
    schemaStore.onChangedSubject("subj", changeFetcher);
    schemaStore.onChangedSubject("obj", changeFetcher);
    schemaStore.finish();

    assertThat(dataStore.getResult(), is("""
        {
          "type" : {
            "name" : "type",
            "predicates" : [ {
              "name" : "langPred",
              "direction" : "OUT",
              "valueTypes" : {
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" : 1
              },
              "referenceTypes" : { },
              "languages" : [ "en" ],
              "subjectsWithThisPredicate" : 1,
              "subjectsWithThisPredicateAsList" : 0,
              "hasBeenList" : false,
              "hasBeenSingular" : true
            }, {
              "name" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
              "direction" : "OUT",
              "valueTypes" : { },
              "referenceTypes" : {
                "http://www.w3.org/2000/01/rdf-schema#Resource" : 1
              },
              "languages" : [ ],
              "subjectsWithThisPredicate" : 1,
              "subjectsWithThisPredicateAsList" : 0,
              "hasBeenList" : false,
              "hasBeenSingular" : true
            }, {
              "name" : "pred",
              "direction" : "OUT",
              "valueTypes" : { },
              "referenceTypes" : {
                "http://www.w3.org/2000/01/rdf-schema#Resource" : 1
              },
              "languages" : [ ],
              "subjectsWithThisPredicate" : 1,
              "subjectsWithThisPredicateAsList" : 0,
              "hasBeenList" : false,
              "hasBeenSingular" : true
            } ],
            "subjectsWithThisType" : 1
          },
          "http://www.w3.org/2000/01/rdf-schema#Resource" : {
            "name" : "http://www.w3.org/2000/01/rdf-schema#Resource",
            "predicates" : [ {
              "name" : "pred",
              "direction" : "IN",
              "valueTypes" : { },
              "referenceTypes" : {
                "type" : 1
              },
              "languages" : [ ],
              "subjectsWithThisPredicate" : 1,
              "subjectsWithThisPredicateAsList" : 0,
              "hasBeenList" : false,
              "hasBeenSingular" : true
            }, {
              "name" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
              "direction" : "IN",
              "valueTypes" : { },
              "referenceTypes" : {
                "type" : 1
              },
              "languages" : [ ],
              "subjectsWithThisPredicate" : 0,
              "subjectsWithThisPredicateAsList" : 0,
              "hasBeenList" : false,
              "hasBeenSingular" : true
            } ],
            "subjectsWithThisType" : 1
          }
        }"""));
  }

  @Test
  public void doNotRemovePredicatesThatDoNotExistOnType() throws Exception {
    final DummyDataStorage dataStore = new DummyDataStorage();

    ChangeFetcher changeFetcher = new DummyChangeFetcher(
      CursorQuad.create("subj", "pred", Direction.OUT, ChangeType.ASSERTED, "obj", RdfConstants.STRING, null, null, "")
    );

    BdbSchemaStore schemaStore = new BdbSchemaStore(dataStore, new ImportStatus(new LogList()));

    schemaStore.start();
    schemaStore.onChangedSubject("subj", changeFetcher);
    schemaStore.finish();

    changeFetcher = new DummyChangeFetcher(
      CursorQuad.create("subj", "pred", Direction.OUT, ChangeType.UNCHANGED,
          "type", RdfConstants.STRING, null, null, ""),
      CursorQuad.create("subj", "pred2", Direction.OUT, ChangeType.ASSERTED,
          "type", RdfConstants.STRING, null, null, ""),
      CursorQuad.create("subj", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
          "type", null, null, null, ""),
      CursorQuad.create("type", RdfConstants.RDF_TYPE, Direction.IN, ChangeType.ASSERTED,
          "subj", null, null, null, "")
    );

    schemaStore.start();
    schemaStore.onChangedSubject("subj", changeFetcher);
    schemaStore.finish();

    Map<String, Type> schema = OBJECT_MAPPER.readValue(dataStore.getResult(), new TypeReference<>() {
    });

    assertThat(schema, hasEntry(is("type"), allOf(
      hasProperty("name", is("type")),
      hasProperty("predicates",
        hasItem(predicate().withName("pred").withDirection(Direction.OUT).withValueType(RdfConstants.STRING))
      ),
      hasProperty("predicates",
        hasItem(predicate().withName("pred2").withDirection(Direction.OUT).withValueType(RdfConstants.STRING))
      )
    )));

    assertThat(schema, hasEntry(
      is("http://www.w3.org/2000/01/rdf-schema#Resource"),
      hasProperty("predicates", not(hasItem(predicate().withName("pred2"))))
    ));
  }

  private static class DummyDataStorage implements DataStorage {
    private String result;

    @Override
    public String getValue() {
      return result;
    }

    @Override
    public void setValue(String newValue) throws DatabaseWriteException {
      result = newValue;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void commit() {

    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public boolean isClean() {
      return true;
    }

    @Override
    public void empty() {

    }

    public String getResult() {
      return result;
    }
  }

  private static class DummyChangeFetcher implements ChangeFetcher {
    private final List<CursorQuad> triples;

    public DummyChangeFetcher(CursorQuad... triples) {
      this.triples = Lists.newArrayList(triples);
    }

    @Override
    public Stream<QuadGraphs> getPredicates(
      String subject,
      boolean getRetracted,
      boolean getUnchanged,
      boolean getAsserted
    ) {
      return QuadGraphs.mapToQuadGraphs(triples.stream()
                    .filter(t -> (t.getChangeType() == ChangeType.RETRACTED) == getRetracted ||
                      (t.getChangeType() == ChangeType.UNCHANGED) == getUnchanged ||
                      (t.getChangeType() == ChangeType.ASSERTED) == getAsserted)
                    .filter(t -> (t.getSubject().equals(subject))));
    }

    @Override
    public Stream<QuadGraphs> getPredicates(
      String subject,
      String predicate,
      Direction direction,
      boolean getRetracted,
      boolean getUnchanged,
      boolean getAsserted
    ) {
      return QuadGraphs.mapToQuadGraphs(triples.stream()
                    .filter(t -> (t.getChangeType() == ChangeType.RETRACTED) == getRetracted ||
                      (t.getChangeType() == ChangeType.UNCHANGED) == getUnchanged ||
                      (t.getChangeType() == ChangeType.ASSERTED) == getAsserted)
                    .filter(t -> (t.getSubject().equals(subject)))
                    .filter(t -> (t.getPredicate().equals(predicate)))
                    .filter(t -> (t.getDirection() == direction)));
    }
  }
}
