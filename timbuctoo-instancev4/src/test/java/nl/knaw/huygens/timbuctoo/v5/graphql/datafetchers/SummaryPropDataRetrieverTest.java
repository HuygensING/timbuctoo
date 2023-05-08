package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.DirectionalPath;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.DirectionalStep;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SimplePath;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SummaryPropDataRetrieverTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());
  private static final String SOURCE = "http://example.org/source";
  private static final String GRAPH = "http://example.org/graph";
  private static final String COLLECTION = "http://example.org/collection";
  private static final String USER_PATH = "http://example.org/userPath";
  private static final String DEFAULT_PATH = "http://example.org/path";
  private static final String USER_CONFIGURED_PATH = "http://example.org/userConfigured";
  private static final String WRONG_COLLECTION = "http://example.org/wrongCollection";
  private static final String WRONG_USER_PATH = "http://example.org/wrongUserPath";
  private static final String DEFAULT_PATH_2 = "http://example.org/path2";
  private static final String OBJECT_1 = "http://example.org/objectFound1";
  private static final String OBJECT_2 = "http://example.org/objectFound2";
  private static final String OBJECT_OF_PATH2 = "http://example.org/objectOfPath2";
  private static final String DEFAULT_PROP_1 = "http://example.org/prop1";
  private static final String DEFAULT_PROP_2 = "http://example.org/prop2";
  private static final String DEFAULT_PROP_3 = "http://example.org/prop3";
  private static final String NOT_TO_BE_RETRIEVED = "http://example.org/objectThatShouldNotBeRetrieved";

  @Test
  public void createSummaryPropertyWalksTheUserConfiguredPathFirst() throws JsonProcessingException {
    List<SummaryProp> defaultProperties = Lists.newArrayList(summaryPropertyWithPath(DEFAULT_PATH));
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad collectionQuad = createCollectionQuad(quadStore, COLLECTION, USER_PATH);
    given(quadStore.getQuadsInGraph(SOURCE, RdfConstants.RDF_TYPE, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(collectionQuad));

    instance.createSummaryProperty(subjectWithUriAndGraph(SOURCE, Optional.of(new Graph(GRAPH))), 
        dataSetWithQuadStore(quadStore), COLLECTION);

    InOrder inOrder = inOrder(quadStore);
    inOrder.verify(quadStore).getQuadsInGraph(SOURCE, RdfConstants.RDF_TYPE, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    // walk the path of the user configured SummaryProp
    inOrder.verify(quadStore).getQuadsInGraph(SOURCE, USER_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    // walk the path of the default SummaryProp
    inOrder.verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
  }

  @Test
  public void createSummaryChoosesTheRightType() throws JsonProcessingException {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath(DEFAULT_PATH)
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      USER_CONFIGURED_PATH,
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad wrongCollection = createCollectionQuad(quadStore, WRONG_COLLECTION, WRONG_USER_PATH);
    CursorQuad collection = createCollectionQuad(quadStore, COLLECTION, USER_PATH);
    given(quadStore.getQuadsInGraph(SOURCE, RdfConstants.RDF_TYPE, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(wrongCollection, collection));

    instance.createSummaryProperty(subjectWithUriAndGraph(SOURCE, Optional.of(new Graph(GRAPH))), 
        dataSetWithQuadStore(quadStore), COLLECTION);

    verify(quadStore, never()).getQuadsInGraph(SOURCE, WRONG_USER_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
  }

  private CursorQuad createCollectionQuad(QuadStore quadStore, String collectionUri, String summaryPropPath)
    throws JsonProcessingException {
    CursorQuad collection = quadWithObject(collectionUri, Optional.empty());
    SummaryProp summaryProp = summaryPropertyWithPath(summaryPropPath);
    CursorQuad userConfiguredSummaryProp =
      quadWithObject(OBJECT_MAPPER.writeValueAsString(summaryProp), Optional.empty());
    given(quadStore.getQuadsInGraph(collectionUri, USER_CONFIGURED_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(userConfiguredSummaryProp));
    return collection;
  }

  @Test
  public void retrieveDefaultPropertyUsesTheFirstQuadFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(summaryPropertyWithPath(DEFAULT_PATH, DEFAULT_PATH_2));
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject(OBJECT_1, Optional.empty());
    CursorQuad foundQuad2 = quadWithObject(OBJECT_2, Optional.empty());
    given(quadStore.getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad objectOfPath2 = quadWithObject(OBJECT_OF_PATH2, Optional.empty());
    given(quadStore.getQuadsInGraph(OBJECT_1, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(objectOfPath2));

    instance.createSummaryProperty(subjectWithUriAndGraph(SOURCE, Optional.of(new Graph(GRAPH))), 
        dataSetWithQuadStore(quadStore), COLLECTION);

    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, "", Optional.of(new Graph(GRAPH)));
    verify(quadStore).getQuadsInGraph(OBJECT_1, DEFAULT_PATH_2, Direction.OUT, "", Optional.of(new Graph(GRAPH)));
    verify(quadStore, never()).getQuadsInGraph(OBJECT_2, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
  }

  @Test
  public void createSummaryPropertyWalksTheSecondPathIfTheFirstGivesNoValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath(DEFAULT_PATH, DEFAULT_PATH_2)
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject(OBJECT_1, Optional.empty());
    CursorQuad foundQuad2 = quadWithObject(OBJECT_2, Optional.empty());
    given(quadStore.getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, "", Optional.of(new Graph(GRAPH))))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad objectOfPath2 = quadWithObject(OBJECT_OF_PATH2, Optional.empty());
    given(quadStore.getQuadsInGraph(OBJECT_2, DEFAULT_PATH_2, Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(objectOfPath2));

    Optional<TypedValue> summaryProperty =
      instance.createSummaryProperty(subjectWithUriAndGraph(
          SOURCE, Optional.of(new Graph(GRAPH))), dataSetWithQuadStore(quadStore), COLLECTION);

    assertThat(summaryProperty, is(present()));
    assertThat(summaryProperty.get(), hasProperty("value", is(OBJECT_OF_PATH2)));

    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore).getQuadsInGraph(OBJECT_1, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore).getQuadsInGraph(OBJECT_2, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
  }

  @Test
  public void createSummaryPropertyIgnoresValueTypesIfThePathIsLongerThanOne() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath(DEFAULT_PATH, DEFAULT_PATH_2)
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      USER_CONFIGURED_PATH,
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject(OBJECT_1, Optional.of(RdfConstants.STRING));
    CursorQuad foundQuad2 = quadWithObject(OBJECT_2, Optional.empty());
    given(quadStore.getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, "", Optional.of(new Graph(GRAPH))))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad notBeRetrieved = quadWithObject(NOT_TO_BE_RETRIEVED, Optional.empty());
    given(quadStore.getQuadsInGraph(OBJECT_1, DEFAULT_PATH_2, Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(notBeRetrieved));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObject(OBJECT_OF_PATH2, Optional.empty());
    given(quadStore.getQuadsInGraph(OBJECT_2, DEFAULT_PATH_2, Direction.OUT,
        "", Optional.of(new Graph(GRAPH))))
      .willReturn(Stream.of(quadWithObjectUriOfPath2));

    Optional<TypedValue> summaryProperty =
      instance.createSummaryProperty(subjectWithUriAndGraph(
          SOURCE, Optional.of(new Graph(GRAPH))), dataSetWithQuadStore(quadStore), COLLECTION);

    assertThat(summaryProperty, is(present()));
    assertThat(summaryProperty.get(), hasProperty("value", is(OBJECT_OF_PATH2)));
    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore).getQuadsInGraph(OBJECT_2, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore, never()).getQuadsInGraph(eq(NOT_TO_BE_RETRIEVED), anyString(), any(Direction.class),
        anyString(), eq(Optional.of(new Graph(GRAPH))));
  }

  @Test
  public void createSummaryPropertyWalksTheWholePathOfTheConfiguredDefaultProperty() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(summaryPropertyWithPath(DEFAULT_PATH, DEFAULT_PATH_2));
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    CursorQuad foundQuad = quadWithObject(OBJECT_1, Optional.empty());
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(foundQuad));

    instance.createSummaryProperty(subjectWithUriAndGraph(
        SOURCE, Optional.of(new Graph(GRAPH))), dataSetWithQuadStore(quadStore), COLLECTION);

    InOrder inOrder = inOrder(quadStore);
    inOrder.verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    inOrder.verify(quadStore).getQuadsInGraph(OBJECT_1, DEFAULT_PATH_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
  }

  @Test
  public void createSummaryPropertyStopsWhenTheFirstPartOfThePathCannotBeFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(summaryPropertyWithPath(DEFAULT_PATH, DEFAULT_PATH_2));
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuadsInGraph(
        SOURCE, DEFAULT_PATH, Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.empty());

    instance.createSummaryProperty(subjectWithUriAndGraph(
        SOURCE, Optional.of(new Graph(GRAPH))), dataSetWithQuadStore(quadStore), COLLECTION);

    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PATH, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore, never()).getQuadsInGraph(anyString(), eq(DEFAULT_PATH_2), eq(Direction.OUT),
        eq(""), eq(Optional.of(new Graph(GRAPH))));
  }

  @Test
  public void createSummaryPropertySearchesUntilItFindsAValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath(DEFAULT_PROP_1),
      summaryPropertyWithPath(DEFAULT_PROP_2),
      summaryPropertyWithPath(DEFAULT_PROP_3)
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObject(OBJECT_1, Optional.empty());
    given(foundQuad.getObject()).willReturn("http://example.org/value");
    given(quadStore.getQuadsInGraph(
        SOURCE, DEFAULT_PROP_2, Direction.OUT,
        "", Optional.of(new Graph(GRAPH)))).willReturn(Stream.of(foundQuad));

    Optional<TypedValue> found =
      instance.createSummaryProperty(subjectWithUriAndGraph(
          SOURCE, Optional.of(new Graph(GRAPH))), dataSetWithQuadStore(quadStore), COLLECTION);

    assertThat(found, is(present()));
    assertThat(found.get(), hasProperty("value", is("http://example.org/value")));
    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PROP_1, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PROP_2, Direction.OUT, 
        "", Optional.of(new Graph(GRAPH)));
    verify(quadStore, never()).getQuadsInGraph(anyString(), eq(DEFAULT_PROP_3), eq(Direction.OUT),
        eq(""), eq(Optional.of(new Graph(GRAPH))));
  }

  @Test
  public void createSummaryPropertySupportsInverseRelations() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(summaryPropertyWithPath(DEFAULT_PROP_1, Direction.IN));
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObject(OBJECT_1, Optional.empty());
    given(foundQuad.getObject()).willReturn("http://example.org/value");

    instance.createSummaryProperty(subjectWithUriAndGraph(SOURCE, Optional.of(new Graph(GRAPH))),
        dataSetWithQuadStore(quadStore), COLLECTION);

    verify(quadStore).getQuadsInGraph(SOURCE, DEFAULT_PROP_1, Direction.IN,
        "", Optional.of(new Graph(GRAPH)));
  }

  @Test
  public void createSummaryPropertyReturnsAnEmptyOptionalWhenNoDefaultPropertiesAreConfigured() {
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(USER_CONFIGURED_PATH, Lists.newArrayList());
    QuadStore quadStore = mock(QuadStore.class);

    Optional<TypedValue> found =
      instance.createSummaryProperty(subjectWithUriAndGraph(SOURCE, Optional.of(new Graph(GRAPH))),
          dataSetWithQuadStore(quadStore), COLLECTION);

    assertThat(found, not(is(present())));
  }

  private DataSet dataSetWithQuadStore(QuadStore quadStore) {
    DataSet dataSet = mock(DataSet.class);
    given(dataSet.getQuadStore()).willReturn(quadStore);
    return dataSet;
  }

  private CursorQuad quadWithObject(String object, Optional<String> valueType) {
    CursorQuad foundQuad = mock(CursorQuad.class);
    given(foundQuad.getObject()).willReturn(object);
    given(foundQuad.getValuetype()).willReturn(valueType);
    given(foundQuad.getGraph()).willReturn(Optional.of(GRAPH));
    return foundQuad;
  }

  private SummaryProp summaryPropertyWithPath(String... path) {
    return SimplePath.create(Lists.newArrayList(path));
  }

  private SummaryProp summaryPropertyWithPath(String predicate, Direction direction) {
    return DirectionalPath.create(Lists.newArrayList(DirectionalStep.create(predicate, direction)));
  }

  private SubjectGraphReference subjectWithUriAndGraph(String uri, Optional<Graph> graph) {
    SubjectGraphReference source = mock(SubjectGraphReference.class);
    given(source.getSubjectUri()).willReturn(uri);
    given(source.getGraph()).willReturn(graph);
    return source;
  }
}
