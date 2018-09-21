package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.util.UserUriCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.VersionStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_VOCAB;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class GraphQlToRdfPatchTest {

  private static final String SUBJECT = "http://example.org/subject";
  private static final String NAMES_FIELD = "schema_name";
  private static final String NAMES_PRED = "http://schema.org/name";
  private static final User USER = mock(User.class);
  private static final String USER_URI = "http://example.org/user";
  private static final String DATA_SET_URI = "http://example.org/dataset";
  private static final String GRAPH_QL_STRING = "xsd_string";
  private DataSet dataSet;
  private QuadStore quadStore;
  private RdfPatchSerializer serializer;
  private UserUriCreator userUriCreator;
  private VersionStore versionStore;
  private DataSetMetaData dataSetMetaData;
  private TypeNameStore typeNameStore;

  @Before
  public void setUp() throws Exception {
    dataSet = mock(DataSet.class);
    quadStore = mock(QuadStore.class);
    versionStore = mock(VersionStore.class);
    userUriCreator = mock(UserUriCreator.class);
    when(userUriCreator.create(USER)).thenReturn(USER_URI);
    dataSetMetaData = mock(DataSetMetaData.class);
    when(dataSetMetaData.getBaseUri()).thenReturn(DATA_SET_URI);
    typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeUri(NAMES_FIELD)).thenReturn(NAMES_PRED);
    when(typeNameStore.makeUri(GRAPH_QL_STRING)).thenReturn(STRING);

    when(dataSet.getQuadStore()).thenReturn(quadStore);
    when(dataSet.getVersionStore()).thenReturn(versionStore);
    when(dataSet.getMetadata()).thenReturn(dataSetMetaData);
    when(dataSet.getTypeNameStore()).thenReturn(typeNameStore);

    serializer = mock(RdfPatchSerializer.class);
  }

  // Update Entity
  @Test
  public void addsValuesForAdditionsToEntity() throws Exception {
    String addedValue = "newValue";
    Map<Object, Object> additions = Maps.newHashMap();
    additions.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(addedValue)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("additions", additions);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, NAMES_PRED, addedValue, STRING, null, null);
  }


  @Test
  public void removesValuesForDeletions() throws Exception {
    String deletedValue = "oldValue";
    Map<Object, Object> deletions = Maps.newHashMap();
    deletions.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(deletedValue)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("deletions", deletions);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(false, SUBJECT, NAMES_PRED, deletedValue, STRING, null, null);
  }

  @Test
  public void replaceSingleValueRemovesTheOldValueAndAddsTheNewValue() throws Exception {
    String newValue = "newValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, createPropertyInput(newValue));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);
    String oldValue = "oldValue";
    valuesInQuadStore(NAMES_PRED, oldValue);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, NAMES_PRED, newValue, STRING, null, null);
    verify(serializer).addDelQuad(false, SUBJECT, NAMES_PRED, oldValue, STRING, null, null);
  }

  @Test
  public void replaceListRemovesAllTheValuesFromTheListAndAddsTheValuesOfTheList() throws Exception {
    String newValue1 = "newValue1";
    String newValue2 = "newValue2";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(newValue1), createPropertyInput(newValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);
    String oldValue1 = "oldValue1";
    String oldValue2 = "oldValue2";
    valuesInQuadStore(NAMES_PRED, oldValue1, oldValue2);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, NAMES_PRED, newValue1, STRING, null, null);
    verify(serializer).addDelQuad(true, SUBJECT, NAMES_PRED, newValue2, STRING, null, null);
    verify(serializer).addDelQuad(false, SUBJECT, NAMES_PRED, oldValue1, STRING, null, null);
    verify(serializer).addDelQuad(false, SUBJECT, NAMES_PRED, oldValue2, STRING, null, null);
  }

  @Test
  public void addsARevisionOfTheEntityToUpdate() throws Exception {
    final String prevRevision = SUBJECT + "/1";
    final String newRevision = SUBJECT + "/2";
    String newValue = "newValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, createPropertyInput(newValue));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);
    String oldValue = "oldValue";
    valuesInQuadStore(NAMES_PRED, oldValue);
    valuesInQuadStore(timPredicate("latestRevision"), prevRevision);
    when(versionStore.getVersion()).thenReturn(1);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(true, SUBJECT, timPredicate("latestRevision"), newRevision, null, null, null);
    verify(serializer)
      .addDelQuad(true, newRevision, "http://www.w3.org/ns/prov#specializationOf", SUBJECT, null, null, null);
    verify(serializer).addDelQuad(true, newRevision, timPredicate("version"), "2", RdfConstants.INTEGER, null, null);
    verify(serializer).addDelQuad(false, SUBJECT, timPredicate("latestRevision"), prevRevision, null, null, null);
  }

  @Test
  public void provenanceIsAddedToTheLatestRevision() throws Exception {
    final String newRevision = SUBJECT + "/2";
    String newValue = "newValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, createPropertyInput(newValue));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);
    String oldValue = "oldValue";
    valuesInQuadStore(NAMES_PRED, oldValue);
    when(versionStore.getVersion()).thenReturn(1);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#generated"),
      eq(newRevision),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Activity"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#associatedWith"),
      eq(USER_URI),
      isNull(),
      isNull(),
      isNull())
    ;
    verify(serializer).addDelQuad(true, USER_URI, RDF_TYPE, "http://www.w3.org/ns/prov#Agent", null, null, null);
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/activity"),
      eq("http://www.w3.org/ns/prov#qualifiedAssociation"),
      startsWith(DATA_SET_URI + "/association"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Association"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq("http://www.w3.org/ns/prov#agent"),
      eq(USER_URI),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/association"),
      eq("http://www.w3.org/ns/prov#hadPlan"),
      startsWith(DATA_SET_URI + "/plan"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(RDF_TYPE),
      eq("http://www.w3.org/ns/prov#Plan"),
      isNull(),
      isNull(),
      isNull()
    );


  }

  @Test
  public void addsAdditionsToPlan() throws Exception {
    String newValue = "newValue";
    String newValue2 = "newValue2";
    Map<Object, Object> additions = Maps.newHashMap();
    additions.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(newValue), createPropertyInput(newValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("additions", additions);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("additions")),
      startsWith(DATA_SET_URI + "/additions"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/additions"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Additions"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/additions"),
      eq(timPredicate("hasAddition")),
      startsWith(DATA_SET_URI + "/addition"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Addition"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(timPredicate("hasKey")),
      eq(NAMES_PRED),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/addition"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue2),
      eq(STRING),
      isNull(),
      isNull()
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
  }


  @Test
  public void addsDeletionsToPlan() throws Exception {
    String delValue = "delValue";
    String delValue2 = "delValue2";
    Map<Object, Object> deletions = Maps.newHashMap();
    deletions.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(delValue), createPropertyInput(delValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("deletions", deletions);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("deletions")),
      startsWith(DATA_SET_URI + "/deletions"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletions"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Deletions"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletions"),
      eq(timPredicate("hasDeletion")),
      startsWith(DATA_SET_URI + "/deletion"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Deletion"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(timPredicate("hasKey")),
      eq(NAMES_PRED),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/deletion"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(delValue),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(delValue2),
      eq(STRING),
      isNull(),
      isNull()
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
  }

  @Test
  public void addsReplacementsToPlan() throws Exception {
    String newValue = "newValue";
    String newValue2 = "newValue2";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(newValue), createPropertyInput(newValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    GraphQlToRdfPatch instance = new GraphQlToRdfPatch(SUBJECT, userUriCreator.create(USER), entity);

    instance.sendQuads(serializer, s -> {
    }, dataSet);

    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/plan"),
      eq(timPredicate("replacements")),
      startsWith(DATA_SET_URI + "/replacements"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacements"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Replacements"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacements"),
      eq(timPredicate("hasReplacement")),
      startsWith(DATA_SET_URI + "/replacement"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(RDF_TYPE),
      eq(TIM_VOCAB + "Replacement"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(timPredicate("hasKey")),
      eq(NAMES_PRED),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/replacement"),
      eq(timPredicate("hasValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
    verify(serializer, times(2)).addDelQuad( // one for each value
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("type")),
      eq(STRING),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue),
      eq(STRING),
      isNull(),
      isNull()
    );
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("rawValue")),
      eq(newValue2),
      eq(STRING),
      isNull(),
      isNull()
    );
    // TODO find a better way to test
    verify(serializer).addDelQuad(
      eq(true),
      startsWith(DATA_SET_URI + "/value"),
      eq(timPredicate("nextValue")),
      startsWith(DATA_SET_URI + "/value"),
      isNull(),
      isNull(),
      isNull()
    );
  }

  // Validation
  @Ignore
  @Test
  public void throwsAnExceptionForDeletionsTriplesThatDoNotExist() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Ignore
  @Test
  public void throwsAnExceptionWhenTheEntityDoesNotExist() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Ignore
  @Test
  public void throwsAnWhenTheDataIsTheWrongType() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  private Map<Object, Object> createPropertyInput(String value) {
    Map<Object, Object> propertyInput = Maps.newHashMap();
    propertyInput.put("type", GRAPH_QL_STRING);
    propertyInput.put("value", value);
    return propertyInput;
  }

  private void valuesInQuadStore(String pred, String... oldValues) {
    List<CursorQuad> quads = Lists.newArrayList();
    for (String oldValue : oldValues) {
      quads.add(CursorQuad.create(SUBJECT, pred, Direction.OUT, oldValue, STRING, null, ""));
    }
    when(quadStore.getQuads(SUBJECT, pred, Direction.OUT, "")).thenReturn(quads.stream());
  }

}
