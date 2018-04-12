package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.DirectionalPath;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.DirectionalStep;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SimplePath;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SummaryPropDataRetrieverTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());

  @Test
  public void createSummaryPropertyWalksTheUserConfiguredPathFirst() throws JsonProcessingException {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    SummaryProp summaryProp = summaryPropertyWithPath("http://example.org/userPath");
    CursorQuad collectionQuad = quadWithObject("http://example.org/collection", Optional.empty());
    CursorQuad userConfiguredSummaryProp = quadWithObject(OBJECT_MAPPER.writeValueAsString(summaryProp),
      Optional.empty());
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuads("http://example.org/source", RdfConstants.RDF_TYPE, Direction.OUT, ""))
      .willReturn(Stream.of(collectionQuad));
    given(quadStore.getQuads("http://example.org/collection", "http://example.org/userConfigured", Direction.OUT, ""))
      .willReturn(Stream.of(userConfiguredSummaryProp));

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    InOrder inOrder = inOrder(quadStore);
    // walk the path of the user configured SummaryProp
    inOrder.verify(quadStore).getQuads("http://example.org/source", "http://example.org/userPath", Direction.OUT, "");
    // walk the path of the default SummaryProp
    inOrder.verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
  }

  @Test
  public void retrieveDefaultPropertyUsesTheFirstQuadFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject("http://example.org/objectFound1", Optional.empty());
    CursorQuad foundQuad2 = quadWithObject("http://example.org/objectFound2", Optional.empty());
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObject("http://example.org/objectOfPath2", Optional.empty());
    given(quadStore.getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, ""))
      .willReturn(Stream.of(quadWithObjectUriOfPath2));

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, "");
    verify(quadStore, never()).getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void createSummaryPropertyWalksTheSecondPathIfTheFirstGivesNoValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject("http://example.org/objectFound1", Optional.empty());
    CursorQuad foundQuad2 = quadWithObject("http://example.org/objectFound2", Optional.empty());
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObject("http://example.org/objectOfPath2", Optional.empty());
    given(quadStore.getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, ""))
      .willReturn(Stream.of(quadWithObjectUriOfPath2));

    Optional<TypedValue> summaryProperty =
      instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    assertThat(summaryProperty, is(present()));
    assertThat(summaryProperty.get(), hasProperty("value", is("http://example.org/objectOfPath2")));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void createSummaryPropertyIgnoresValueTypesIfThePathIsLongerThanOne() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObject("http://example.org/objectFound1", Optional.of(RdfConstants.STRING));
    CursorQuad foundQuad2 = quadWithObject("http://example.org/objectFound2", Optional.empty());
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad quadThatShouldNotBeFound = quadWithObject(
      "http://example.org/objectThatShouldNotBeRetrieved",
      Optional.empty()
    );
    given(quadStore.getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, ""))
      .willReturn(Stream.of(quadThatShouldNotBeFound));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObject("http://example.org/objectOfPath2", Optional.empty());
    given(quadStore.getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, ""))
      .willReturn(Stream.of(quadWithObjectUriOfPath2));

    Optional<TypedValue> summaryProperty =
      instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    assertThat(summaryProperty, is(present()));
    assertThat(summaryProperty.get(), hasProperty("value", is("http://example.org/objectOfPath2")));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void createSummaryPropertyWalksTheWholePathOfTheConfiguredDefaultProperty() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    CursorQuad foundQuad = quadWithObject("http://example.org/objectFound", Optional.empty());
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad));

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    InOrder inOrder = inOrder(quadStore);
    inOrder.verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    inOrder.verify(quadStore).getQuads("http://example.org/objectFound", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void createSummaryPropertyStopsWhenTheFirstPartOfThePathCannotBeFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.empty());

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore, never()).getQuads(anyString(), eq("http://example.org/path2"), eq(Direction.OUT), eq(""));
  }

  @Test
  public void createSummaryPropertySearchesUntilItFindsAValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/prop1"),
      summaryPropertyWithPath("http://example.org/prop2"),
      summaryPropertyWithPath("http://example.org/prop3")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObject("http://example.org/objectFound1", Optional.empty());
    given(foundQuad.getObject()).willReturn("http://example.org/value");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/prop2", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad));

    Optional<TypedValue> found =
      instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));


    assertThat(found, is(present()));
    assertThat(found.get(), hasProperty("value", is("http://example.org/value")));
    verify(quadStore).getQuads("http://example.org/source", "http://example.org/prop1", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/source", "http://example.org/prop2", Direction.OUT, "");
    verify(quadStore, never()).getQuads(anyString(), eq("http://example.org/prop3"), eq(Direction.OUT), eq(""));
  }

  @Test
  public void createSummaryPropertySupportsInverseRelations() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/prop1", Direction.IN)
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObject("http://example.org/objectFound1", Optional.empty());
    given(foundQuad.getObject()).willReturn("http://example.org/value");

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/prop1", Direction.IN, "");
  }

  @Test
  public void createSummaryPropertyReturnsAnEmptyOptionalWhenNoDefaultPropertiesAreConfigured() {
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      Lists.newArrayList()
    );
    QuadStore quadStore = mock(QuadStore.class);

    Optional<TypedValue> found =
      instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));


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
    return foundQuad;
  }

  private SummaryProp summaryPropertyWithPath(String... path) {
    return SimplePath.create(Lists.newArrayList(path));
  }

  private SummaryProp summaryPropertyWithPath(String predicate, Direction direction) {
    return DirectionalPath.create(Lists.newArrayList(DirectionalStep.create(predicate, direction)));
  }


  private SubjectReference subjectWithUri(String uri) {
    SubjectReference source = mock(SubjectReference.class);
    given(source.getSubjectUri()).willReturn(uri);
    return source;
  }

}
