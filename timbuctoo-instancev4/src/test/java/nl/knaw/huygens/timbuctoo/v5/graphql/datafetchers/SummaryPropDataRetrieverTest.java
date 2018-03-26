package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.rml.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
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
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SummaryPropDataRetrieverTest {

  @Test
  public void createSummaryPropertyWalksTheUserConfiguredPathFirst() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    CursorQuad foundQuad = quadWithObjectUri("http://example.org/objectFound");
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad));

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    InOrder inOrder = inOrder(quadStore);
    inOrder.verify(quadStore).getQuads("http://example.org/source", "http://example.org/userConfigured", Direction.OUT, "");
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
    CursorQuad foundQuad1 = quadWithObjectUri("http://example.org/objectFound1");
    CursorQuad foundQuad2 = quadWithObjectUri("http://example.org/objectFound2");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObjectUri("http://example.org/objectOfPath2");
    given(quadStore.getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, ""))
      .willReturn(Stream.of(quadWithObjectUriOfPath2));

    instance.createSummaryProperty(subjectWithUri("http://example.org/source"), dataSetWithQuadStore(quadStore));

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, "");
    verify(quadStore, never()).getQuads("http://example.org/objectFound2", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void retrieveDefaultPropertyWalksTheSecondPathIfTheFirstGivesNoValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObjectUri("http://example.org/objectFound1");
    CursorQuad foundQuad2 = quadWithObjectUri("http://example.org/objectFound2");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Stream.of(foundQuad1, foundQuad2));
    CursorQuad quadWithObjectUriOfPath2 = quadWithObjectUri("http://example.org/objectOfPath2");
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
  public void createSummaryPropertyWalksTheWholePathOfTheConfiguredDefaultProperty() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    SummaryPropDataRetriever instance = new SummaryPropDataRetriever(
      "http://example.org/userConfigured",
      defaultProperties
    );
    CursorQuad foundQuad = quadWithObjectUri("http://example.org/objectFound");
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
    CursorQuad foundQuad = quadWithObjectUri("http://example.org/objectFound1");
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

  private CursorQuad quadWithObjectUri(String objectUri) {
    CursorQuad foundQuad = mock(CursorQuad.class);
    given(foundQuad.getObject()).willReturn(objectUri);
    return foundQuad;
  }

  private SummaryProp summaryPropertyWithPath(String... path) {
    return SummaryProp.create(Lists.newArrayList(path), "SimplePath");
  }

  private SubjectReference subjectWithUri(String uri) {
    SubjectReference source = mock(SubjectReference.class);
    given(source.getSubjectUri()).willReturn(uri);
    return source;
  }

}
