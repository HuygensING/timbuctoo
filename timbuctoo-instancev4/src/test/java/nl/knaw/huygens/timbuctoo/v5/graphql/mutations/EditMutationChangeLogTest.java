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
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EditMutationChangeLogTest {
  private static final String SUBJECT = "http://example.org/subject";
  private static final String NAMES_FIELD = "schema_name";
  private static final String NAMES_PRED = "http://schema.org/name";
  private static final User USER = mock(User.class);
  private static final String USER_URI = "http://example.org/user";
  private static final String DATA_SET_URI = "http://example.org/dataset";
  private static final String GRAPH_QL_STRING = "xsd_string";
  private DataSet dataSet;
  private QuadStore quadStore;
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
    when(typeNameStore.makeUriForPredicate(NAMES_FIELD)).thenReturn(Optional.of(tuple(NAMES_PRED, Direction.OUT)));
    when(typeNameStore.makeUri(GRAPH_QL_STRING)).thenReturn(STRING);

    when(dataSet.getQuadStore()).thenReturn(quadStore);
    when(dataSet.getVersionStore()).thenReturn(versionStore);
    when(dataSet.getMetadata()).thenReturn(dataSetMetaData);
    when(dataSet.getTypeNameStore()).thenReturn(typeNameStore);
  }

  @Test
  public void getAdditionsReturnsAllAdditions() throws Exception {
    String addedValue = "newValue";
    Map<Object, Object> additions = Maps.newHashMap();
    additions.put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(addedValue)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("additions", additions);
    EditMutationChangeLog instance = new EditMutationChangeLog(entity);

    Stream<Change> adds = instance.getAdditions(SUBJECT, dataSet);

    assertThat(adds.findFirst().get(), is(new Change(SUBJECT, NAMES_PRED, addedValue, STRING, null, null)));
  }

  @Test
  public void getAdditionsReturnsAllReplacementsWithoutOldValue() throws Exception {
    String addedValue = "newValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, createPropertyInput(addedValue));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    EditMutationChangeLog instance = new EditMutationChangeLog(entity);

    Stream<Change> adds = instance.getAdditions(SUBJECT, dataSet);

    assertThat(adds.findFirst().get(), is(new Change(SUBJECT, NAMES_PRED, addedValue, STRING, null, null)));
  }

  @Test
  public void getAdditionsReturnsEmptyStreamForReplacementsWithOldValue() throws Exception {
    String addedValue = "newValue";
    String oldValue = "oldValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements.put(NAMES_FIELD, createPropertyInput(addedValue));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    EditMutationChangeLog instance = new EditMutationChangeLog(entity);

    valuesInQuadStore(NAMES_PRED, oldValue);

    Stream<Change> adds = instance.getAdditions(SUBJECT, dataSet);

    assertThat(adds.findFirst().isPresent(), is(false));
  }

  @Test
  public void getAdditionsReturnsAllReplacementsListsWithoutOldValue() throws Exception {
    String addedValue1 = "newValue1";
    String addedValue2 = "newValue2";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements
      .put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(addedValue1), createPropertyInput(addedValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    EditMutationChangeLog instance = new EditMutationChangeLog(entity);

    List<Change> adds = instance.getAdditions(SUBJECT, dataSet).collect(Collectors.toList());

    assertThat(adds, containsInAnyOrder(
      new Change(SUBJECT, NAMES_PRED, addedValue1, STRING, null, null),
      new Change(SUBJECT, NAMES_PRED, addedValue2, STRING, null, null)
    ));
  }

  @Test
  public void getAdditionsReturnsEmptyStreamForReplacementsListsWithOldValue() throws Exception {
    String addedValue1 = "newValue1";
    String addedValue2 = "newValue2";
    String oldValue = "oldValue";
    Map<Object, Object> replacements = Maps.newHashMap();
    replacements
      .put(NAMES_FIELD, Lists.newArrayList(createPropertyInput(addedValue1), createPropertyInput(addedValue2)));
    Map<Object, Object> entity = Maps.newHashMap();
    entity.put("replacements", replacements);
    EditMutationChangeLog instance = new EditMutationChangeLog(entity);

    valuesInQuadStore(NAMES_PRED, oldValue);

    Stream<Change> adds = instance.getAdditions(SUBJECT, dataSet);

    assertThat(adds.findFirst().isPresent(), is(false));
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