package nl.knaw.huygens.timbuctoo.graphql.mutations;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.mutations.Change.Value;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.ChangeLog;
import nl.knaw.huygens.timbuctoo.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.util.Graph;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.TIM_VOCAB;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.timPredicate;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.timType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class GraphQlToRdfPatchTest {
  private static final String PRED2 = "http://example.org/pred2";
  private static final String PRED1 = "http://example.org/pred1";
  private static final String GRAPH = "http://example.org/graph";
  private static final String SUBJECT = "http://example.org/subject";
  private static final String DATA_SET_URI = "http://example.org/dataset";
  private static final String USER_URI = "http://example.org/user";

  private RdfPatchSerializer serializer;
  private ChangeLog changeLog;
  private DataSet dataSet;
  private QuadStore quadStore;

  @BeforeEach
  public void setUp() {
    serializer = mock(RdfPatchSerializer.class);
    changeLog = mock(ChangeLog.class);
    dataSet = mock(DataSet.class);
    quadStore = mock(QuadStore.class);

    DataSetMetaData dataSetMetaData = mock(DataSetMetaData.class);
    when(dataSetMetaData.getBaseUri()).thenReturn(DATA_SET_URI);
    when(dataSet.getMetadata()).thenReturn(dataSetMetaData);
    when(dataSet.getQuadStore()).thenReturn(quadStore);
    when(quadStore.getQuadsInGraph(SUBJECT, timPredicate("latestRevision"), Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).thenReturn(Stream.empty());
  }

  @Test
  public void addsQuadsForEachAdditionsToEntity() throws Exception {
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    addAdditionsToChangeLog(
      new Change(new Graph(GRAPH), SUBJECT, PRED1, newArrayList(new Value(addedValue, STRING)), null),
      new Change(new Graph(GRAPH), SUBJECT, PRED2, newArrayList(new Value(addedValue2, STRING)), null));
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
    verify(serializer).addDelQuad(true, SUBJECT, PRED2, addedValue2, STRING, null, GRAPH);
  }

  @Test
  public void addsQuadsForEachValueInAnAddition() throws Exception {
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    addAdditionsToChangeLog(
      new Change(new Graph(GRAPH), SUBJECT, PRED1,
          newArrayList(new Value(addedValue, STRING), new Value(addedValue2, STRING)), null)
    );
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
    verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue2, STRING, null, GRAPH);
  }

  @Test
  public void addsTheNewValuesForReplacement() throws Exception {
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    String oldValue = "oldValue";
    addReplacementsToChangeLog(new Replacement(
      PRED1,
      newArrayList(new Value(addedValue, STRING), new Value(addedValue2, STRING)),
      newArrayList(new Value(oldValue, STRING))
    ));
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    InOrder inOrder = inOrder(serializer);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue2, STRING, null, GRAPH);
  }

  @Test
  public void addsAndRemovesValuesOfAllReplacements() throws Exception {
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    String oldValue = "oldValue";
    String oldValue2 = "oldValue2";
    addReplacementsToChangeLog(
      new Replacement(
        PRED1,
        newArrayList(new Value(addedValue, STRING)),
        newArrayList(new Value(oldValue, STRING))
      ),
      new Replacement(
        PRED2,
        newArrayList(new Value(addedValue2, STRING)),
        newArrayList(new Value(oldValue2, STRING))
      )
    );
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    InOrder inOrder = inOrder(serializer);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED2, oldValue2, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED2, addedValue2, STRING, null, GRAPH);
  }

  @Test
  public void removesAllOldValuesOfReplacements() throws Exception {
    String oldValue = "oldValue";
    String oldValue2 = "oldValue2";
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    addReplacementsToChangeLog(new Replacement(
      PRED1,
      newArrayList(new Value(addedValue, STRING), new Value(addedValue2, STRING)),
      newArrayList(new Value(oldValue, STRING), new Value(oldValue2, STRING))
    ));
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    InOrder inOrder = inOrder(serializer);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue2, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue2, STRING, null, GRAPH);
  }

  @Test
  public void deletesAllValuesOfTheDeletions() throws Exception {
    String oldValue = "oldValue";
    String oldValue2 = "oldValue2";
    addDeletionsToChangeLog(new Deletion(
      PRED1,
      newArrayList(new Value(oldValue, STRING), new Value(oldValue2, STRING))
    ));
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue2, STRING, null, GRAPH);
  }

  @Test
  public void removesOldValuesOfAllDeletions() throws Exception {
    String oldValue = "oldValue";
    String oldValue2 = "oldValue2";
    addDeletionsToChangeLog(
      new Deletion(
        PRED1,
        newArrayList(new Value(oldValue, STRING))
      ),
      new Deletion(
        PRED2,
        newArrayList(new Value(oldValue2, STRING))
      )
    );
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    verify(serializer).addDelQuad(false, SUBJECT, PRED2, oldValue2, STRING, null, GRAPH);
  }

  @Test
  public void deletionsAreExecutedBeforeAdditions() throws Exception {
    String addedValue = "newValue";
    String oldValue = "oldValue";
    addAdditionsToChangeLog(
      new Change(
          new Graph(GRAPH),
        SUBJECT,
        PRED1,
        newArrayList(new Value(addedValue, STRING)), null)
    );
    addDeletionsToChangeLog(
      new Deletion(
        PRED1,
        newArrayList(new Value(oldValue, STRING))
      )
    );
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    InOrder inOrder = inOrder(serializer);
    inOrder.verify(serializer).addDelQuad(false, SUBJECT, PRED1, oldValue, STRING, null, GRAPH);
    inOrder.verify(serializer).addDelQuad(true, SUBJECT, PRED1, addedValue, STRING, null, GRAPH);
  }

  // add revision
  @Test
  public void addsRevisionOfTheUpdatedEntity() throws Exception {
    final int prevVersion = -1;
    final int newVersion = 0;

    final String prevRevision = SUBJECT + "/" + prevVersion;
    final String newRevision = SUBJECT + "/" + newVersion;
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, timPredicate("latestRevision"), newRevision, null, null, GRAPH);
    verify(serializer)
      .addDelQuad(true, newRevision, "http://www.w3.org/ns/prov#specializationOf", SUBJECT, null, null, GRAPH);
    verify(serializer).addDelQuad(
      true,
      newRevision,
      timPredicate("version"),
      String.valueOf(newVersion),
      RdfConstants.INTEGER,
      null,
      GRAPH
    );
    verify(serializer).addDelQuad(false, SUBJECT, timPredicate("latestRevision"), prevRevision, null, null, GRAPH);
  }

  @Test
  public void updatesVersionBasedOnPreviousVersion() throws Exception {
    final int prevVersion = 4;
    final int newVersion = 5;

    final String prevRevision = SUBJECT + "/" + prevVersion;
    final String newRevision = SUBJECT + "/" + newVersion;
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    when(quadStore.getQuads(SUBJECT, timPredicate("latestRevision"), Direction.OUT, ""))
      .thenAnswer((Answer<Stream<CursorQuad>>) invocation -> {
        List<CursorQuad> quads = newArrayList();
        quads.add(
          CursorQuad.create(SUBJECT, timPredicate("latestRevision"), Direction.OUT,
              prevRevision, STRING, null, GRAPH, ""));
        return quads.stream();
      });

    when(quadStore.getQuads(prevRevision, timPredicate("version"), Direction.OUT, ""))
      .thenAnswer((Answer<Stream<CursorQuad>>) invocation -> {
        List<CursorQuad> quads = newArrayList();
        quads.add(CursorQuad
          .create(prevRevision, timPredicate("version"), Direction.OUT,
              String.valueOf(prevVersion), INTEGER, null, GRAPH,""));
        return quads.stream();
      });

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer)
      .addDelQuad(true, newRevision, timPredicate("version"), String.valueOf(newVersion),
          RdfConstants.INTEGER, null, GRAPH);
  }

  // add provenance
  @Test
  public void provenanceIsAddedToTheLatestRevision() throws Exception {
    final int newVersion = 0;

    final String newRevision = SUBJECT + "/" + newVersion;
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#generated"),
      eq(newRevision),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Activity"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#associatedWith"),
      eq(USER_URI),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(true, USER_URI, RDF_TYPE, "http://www.w3.org/ns/prov#Agent", null, null, GRAPH);
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#qualifiedAssociation"),
      startsWith(DATA_SET_URI + "/association"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Association"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq("http://www.w3.org/ns/prov#agent"),
      eq(USER_URI),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq("http://www.w3.org/ns/prov#hadPlan"),
      startsWith(DATA_SET_URI + "/plan"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Plan"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
  }

  @Test
  public void addsAdditionsToPlan() throws Exception {
    String addedValue = "newValue";
    String addedValue2 = "newValue2";
    addAdditionsToChangeLog(
      new Change(new Graph(GRAPH), SUBJECT, PRED1,
          newArrayList(new Value(addedValue, STRING), new Value(addedValue2, STRING)), null)
    );

    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("additions")),
      startsWith(DATA_SET_URI + "/additions"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/additions"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Additions"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/additions"),
      eq(timPredicate("hasAddition")),
      startsWith(DATA_SET_URI + "/addition"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Addition"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(timPredicate("hasKey")),
      eq(PRED1),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(RDF_TYPE),
      eq(timType("Value")),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(addedValue),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(addedValue2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
  }

  @Test
  public void addsDeletionsToPlan() throws Exception {
    String oldValue = "oldValue";
    String oldValue2 = "oldValue2";
    addDeletionsToChangeLog(new Deletion(
      PRED1,
      newArrayList(new Value(oldValue, STRING), new Value(oldValue2, STRING))
    ));
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("deletions")),
      startsWith(DATA_SET_URI + "/deletions"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletions"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Deletions"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletions"),
      eq(timPredicate("hasDeletion")),
      startsWith(DATA_SET_URI + "/deletion"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Deletion"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(timPredicate("hasKey")),
      eq(PRED1),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(RDF_TYPE),
      eq(timType("Value")),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(oldValue),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(oldValue2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
  }

  @Test
  public void addsReplacementsToPlan() throws Exception {
    String newValue = "newValue";
    String newValue2 = "newValue2";
    String oldValue = "oldValue1";
    String oldValue2 = "oldValue2";

    addReplacementsToChangeLog(
      new Replacement(
        PRED1,
        newArrayList(new Value(newValue, STRING), new Value(newValue2, STRING)),
        newArrayList(new Value(oldValue, STRING), new Value(oldValue2, STRING))
      ));

    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("replacements")),
      startsWith(DATA_SET_URI + "/replacements"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacements"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Replacements"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacements"),
      eq(timPredicate("hasReplacement")),
      startsWith(DATA_SET_URI + "/replacement"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Replacement"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(timPredicate("hasKey")),
      eq(PRED1),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    // new values
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    // old values
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(timPredicate("hadValue")),
      startsWith(DATA_SET_URI + "/oldValue"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/oldValue"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/oldValue"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "OldValue"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/oldValue"),
      eq(timPredicate("rawValue")),
      eq(oldValue),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/oldValue"),
      eq(timPredicate("rawValue")),
      eq(oldValue2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/oldValue"),
      eq(timPredicate("nextOldValue")),
      startsWith(DATA_SET_URI + "/oldValue"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
  }

  @Test
  public void addsCustomProvenanceToPlan() throws Exception {
    String value1 = "value1";
    String value2 = "value2";
    addProvenanceToChangeLog(
      new Provenance(PRED1, newArrayList(new Value(value1, STRING))),
      new Provenance(PRED2, newArrayList(new Value(value2, STRING)))
    );
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(GRAPH, SUBJECT, USER_URI, changeLog);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("hasCustomProv")),
      startsWith(DATA_SET_URI + "/customProv"),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/customProv"),
      eq(RDF_TYPE),
      eq(timType("CustomProv")),
      isNull(),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(SUBJECT),
      eq(PRED1),
      eq(value1),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(SUBJECT),
      eq(PRED2),
      eq(value2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/customProv"),
      eq(PRED1),
      eq(value1),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/customProv"),
      eq(PRED2),
      eq(value2),
      eq(STRING),
      isNull(),
      eq(GRAPH)
    );
  }

  private void addAdditionsToChangeLog(Change... changes) {
    when(changeLog.getAdditions(any(DataSet.class))).thenAnswer(
        (Answer<Stream<Change>>) invocation -> newArrayList(changes).stream());
  }

  private void addDeletionsToChangeLog(Deletion... deletions) {
    when(changeLog.getDeletions(any(DataSet.class))).thenAnswer(
        (Answer<Stream<Change>>) invocation -> newArrayList(deletions).stream().map(Deletion::toChange));
  }

  private record Deletion(String predicate, List<Value> valuesToDelete) {
    private Change toChange() {
      return new Change(new Graph(GRAPH), SUBJECT, predicate, newArrayList(), valuesToDelete.stream());
    }
  }

  private void addReplacementsToChangeLog(Replacement... replacements) {
    when(changeLog.getReplacements(any(DataSet.class))).thenAnswer(
        (Answer<Stream<Change>>) invocation -> newArrayList(replacements).stream().map(Replacement::toChange));
  }

  private record Replacement(String predicate, List<Value> newValues, List<Value> oldValues) {
    private Change toChange() {
      return new Change(new Graph(GRAPH), SUBJECT, predicate, newValues, oldValues.stream());
    }
  }

  private void addProvenanceToChangeLog(Provenance... provenances) {
    Answer<Stream<Change>> answer = invocation -> newArrayList(provenances).stream().flatMap(
      provenance -> Arrays.stream(invocation.getArguments()).skip(1)
        .map(arg -> provenance.toChange(arg.toString())));

    when(changeLog.getProvenance(any(DataSet.class), any(String.class))).thenAnswer(answer);
    when(changeLog.getProvenance(any(DataSet.class), any(String.class), any(String.class))).thenAnswer(answer);
  }

  private record Provenance(String predicate, List<Value> values) {
    private Change toChange(String subject) {
      return new Change(new Graph(GRAPH), subject, predicate, values, Stream.empty());
    }
  }
}
