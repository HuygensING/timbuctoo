package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BdbSchemaStoreTest {
  @Test
  public void generatesASchemaForAllAdded() throws Exception {
    final DummyDataStorage dataStore = new DummyDataStorage();

    ChangeFetcher changeFetcher = new DummyChangeFetcher(
      CursorQuad.create("subj", "pred", Direction.OUT, ChangeType.ASSERTED, "obj", null, null, ""),
      CursorQuad.create("subj", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED, "type", null, null, ""),
      CursorQuad.create("obj", "pred", Direction.IN, ChangeType.ASSERTED, "subj", null, null, "")
    );

    BdbSchemaStore schemaStore = new BdbSchemaStore(dataStore, new ImportStatus(new LogList()));

    schemaStore.start();
    schemaStore.onChangedSubject("subj", changeFetcher);
    schemaStore.onChangedSubject("obj", changeFetcher);
    schemaStore.finish();

    assertThat(dataStore.getResult(), is("{\n" +
      "  \"http://timbuctoo.huygens.knaw.nl/static/v5/vocabulary#unknown\" : {\n" +
      "    \"name\" : \"http://timbuctoo.huygens.knaw.nl/static/v5/vocabulary#unknown\",\n" +
      "    \"predicates\" : [ {\n" +
      "      \"name\" : \"pred\",\n" +
      "      \"direction\" : \"IN\",\n" +
      "      \"valueTypes\" : { },\n" +
      "      \"referenceTypes\" : {\n" +
      "        \"type\" : 1\n" +
      "      },\n" +
      "      \"subjectsWithThisPredicate\" : 1,\n" +
      "      \"subjectsWithThisPredicateAsList\" : 0,\n" +
      "      \"optional\" : false,\n" +
      "      \"hasBeenList\" : false,\n" +
      "      \"hasBeenSingular\" : true\n" +
      "    } ],\n" +
      "    \"subjectsWithThisType\" : 1\n" +
      "  },\n" +
      "  \"type\" : {\n" +
      "    \"name\" : \"type\",\n" +
      "    \"predicates\" : [ {\n" +
      "      \"name\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\",\n" +
      "      \"direction\" : \"OUT\",\n" +
      "      \"valueTypes\" : { },\n" +
      "      \"referenceTypes\" : {\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/static/v5/vocabulary#unknown\" : 1\n" +
      "      },\n" +
      "      \"subjectsWithThisPredicate\" : 1,\n" +
      "      \"subjectsWithThisPredicateAsList\" : 0,\n" +
      "      \"optional\" : false,\n" +
      "      \"hasBeenList\" : false,\n" +
      "      \"hasBeenSingular\" : true\n" +
      "    }, {\n" +
      "      \"name\" : \"pred\",\n" +
      "      \"direction\" : \"OUT\",\n" +
      "      \"valueTypes\" : { },\n" +
      "      \"referenceTypes\" : {\n" +
      "        \"http://timbuctoo.huygens.knaw.nl/static/v5/vocabulary#unknown\" : 1\n" +
      "      },\n" +
      "      \"subjectsWithThisPredicate\" : 1,\n" +
      "      \"subjectsWithThisPredicateAsList\" : 0,\n" +
      "      \"optional\" : false,\n" +
      "      \"hasBeenList\" : false,\n" +
      "      \"hasBeenSingular\" : true\n" +
      "    } ],\n" +
      "    \"subjectsWithThisType\" : 1\n" +
      "  }\n" +
      "}"));
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
    public Stream<CursorQuad> getPredicates(
      String subject,
      boolean getRetracted,
      boolean getUnchanged,
      boolean getAsserted
    ) {
      return triples.stream()
        .filter(t -> (t.getChangeType() == ChangeType.RETRACTED) == getRetracted ||
          (t.getChangeType() == ChangeType.UNCHANGED) == getUnchanged ||
          (t.getChangeType() == ChangeType.ASSERTED) == getAsserted)
        .filter(t -> (t.getSubject().equals(subject)));
    }

    @Override
    public Stream<CursorQuad> getPredicates(
      String subject,
      String predicate,
      Direction direction,
      boolean getRetracted,
      boolean getUnchanged,
      boolean getAsserted
    ) {
      return triples.stream()
        .filter(t -> (t.getChangeType() == ChangeType.RETRACTED) == getRetracted ||
          (t.getChangeType() == ChangeType.UNCHANGED) == getUnchanged ||
          (t.getChangeType() == ChangeType.ASSERTED) == getAsserted)
        .filter(t -> (t.getSubject().equals(subject)))
        .filter(t -> (t.getPredicate().equals(predicate)))
        .filter(t -> (t.getDirection() == direction));
    }
  }
}
