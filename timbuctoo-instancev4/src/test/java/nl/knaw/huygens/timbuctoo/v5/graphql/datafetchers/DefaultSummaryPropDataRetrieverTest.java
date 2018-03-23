package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class DefaultSummaryPropDataRetrieverTest {

  @Test
  public void retrieveDefaultPropertyWalksThePathOfTheConfiguredDefaultProperty() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    DefaultSummaryPropDataRetriever instance = new DefaultSummaryPropDataRetriever(defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObjectUri("http://example.org/objectFound");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Lists.newArrayList(foundQuad).stream());

    SubjectReference source = subjectWithUri("http://example.org/source");

    instance.retrieveDefaultProperty(source, quadStore);

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound", "http://example.org/path2", Direction.OUT, "");
  }

  @Test
  public void retrieveDefaultPropertyStopsWhenTheFirstPartOfThePathCannotBeFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    DefaultSummaryPropDataRetriever instance = new DefaultSummaryPropDataRetriever(defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Lists.<CursorQuad>newArrayList().stream());

    instance.retrieveDefaultProperty(subjectWithUri("http://example.org/source"), quadStore);

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verifyNoMoreInteractions(quadStore);
  }

  @Test
  public void retrieveDefaultPropertyUsesTheFirstQuadFound() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/path", "http://example.org/path2")
    );
    DefaultSummaryPropDataRetriever instance = new DefaultSummaryPropDataRetriever(defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad1 = quadWithObjectUri("http://example.org/objectFound1");
    CursorQuad foundQuad2 = quadWithObjectUri("http://example.org/objectFound2");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, ""))
      .willReturn(Lists.newArrayList(foundQuad1, foundQuad2).stream());

    instance.retrieveDefaultProperty(subjectWithUri("http://example.org/source"), quadStore);

    verify(quadStore).getQuads("http://example.org/source", "http://example.org/path", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/objectFound1", "http://example.org/path2", Direction.OUT, "");
    verifyNoMoreInteractions(quadStore);
  }


  @Test
  public void retrieveDefaultPropertySearchesUntilItFindsAValue() {
    List<SummaryProp> defaultProperties = Lists.newArrayList(
      summaryPropertyWithPath("http://example.org/prop1"),
      summaryPropertyWithPath("http://example.org/prop2"),
      summaryPropertyWithPath("http://example.org/prop3")
    );
    DefaultSummaryPropDataRetriever instance = new DefaultSummaryPropDataRetriever(defaultProperties);
    QuadStore quadStore = mock(QuadStore.class);
    CursorQuad foundQuad = quadWithObjectUri("http://example.org/objectFound1");
    given(quadStore.getQuads("http://example.org/source", "http://example.org/prop2", Direction.OUT, ""))
      .willReturn(Lists.newArrayList(foundQuad).stream());

    Optional<CursorQuad> found =
      instance.retrieveDefaultProperty(subjectWithUri("http://example.org/source"), quadStore);


    assertThat(found, is(present()));
    assertThat(found.get(), is(sameInstance(foundQuad)));
    verify(quadStore).getQuads("http://example.org/source", "http://example.org/prop1", Direction.OUT, "");
    verify(quadStore).getQuads("http://example.org/source", "http://example.org/prop2", Direction.OUT, "");
    verifyNoMoreInteractions(quadStore);
  }

  @Test
  public void retrieveDefaultPropertyReturnsAnEmptyOptionWhenNoDefaultPropertiesAreConfigured() {
    DefaultSummaryPropDataRetriever instance = new DefaultSummaryPropDataRetriever(Lists.newArrayList());
    QuadStore quadStore = mock(QuadStore.class);

    Optional<CursorQuad> found =
      instance.retrieveDefaultProperty(subjectWithUri("http://example.org/source"), quadStore);


    assertThat(found, not(is(present())));
    verifyZeroInteractions(quadStore);
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
