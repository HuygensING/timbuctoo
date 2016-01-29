package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.search.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.search.VertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AbstractSearchDescriptionTest {

  @Test
  public void executeCreatesASearchResult() {
    AbstractSearchDescription instance = searchDescription().build();

    SearchResult searchResult = instance.execute(newGraph().build(), new SearchRequestV2_1());

    assertThat(searchResult, is(Matchers.notNullValue()));
  }

  // TODO extract ref creator class
  @Test
  public void executeCreatesARefForEachLatestVertexWithTheRightType() {
    PropertyDescriptor idDescriptor = mock(PropertyDescriptor.class);
    PropertyDescriptor displayNameDescriptor = mock(PropertyDescriptor.class);
    PropertyDescriptor dataDescriptor1 = mock(PropertyDescriptor.class);
    PropertyDescriptor dataDescriptor2 = mock(PropertyDescriptor.class);
    String type = "type";
    AbstractSearchDescription instance = searchDescription()
      .withType(type)
      .withIdDescriptor(idDescriptor)
      .withDisplayNameDescriptor(displayNameDescriptor)
      .withDataDescriptor("desc1", dataDescriptor1)
      .withDataDescriptor("desc2", dataDescriptor2)
      .build();
    Graph graph = newGraph()
      .withVertex(vertex().withId("id").isLatest(true).withType(type))
      .withVertex(vertex().withId("id1").isLatest(true).withType("otherType"))
      .withVertex(vertex().withId("id").isLatest(false).withType(type))
      .build();

    SearchResult searchResult = instance.execute(graph, new SearchRequestV2_1());

    assertThat(searchResult.getRefs(), is(not(empty())));

    verify(idDescriptor, times(1)).get(argThat(likeVertex().withType(type)));
    verify(displayNameDescriptor, times(1)).get(argThat(likeVertex().withType(type)));
    verify(dataDescriptor1, times(1)).get(argThat(likeVertex().withType(type)));
    verify(dataDescriptor2, times(1)).get(argThat(likeVertex().withType(type)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void createFacetsLetsEachFacetDescriptionFillAListOfFacets() {
    FacetDescription facetDescription1 = mock(FacetDescription.class);
    FacetDescription facetDescription2 = mock(FacetDescription.class);
    String type = "type";
    AbstractSearchDescription instance = searchDescription()
      .withType(type)
      .withFacetDescription(facetDescription1)
      .withFacetDescription(facetDescription2)
      .build();
    Graph graph = newGraph()
      .withVertex(vertex().withId("id").isLatest(true).withType(type))
      .withVertex(vertex().withId("id1").isLatest(true).withType("otherType"))
      .withVertex(vertex().withId("id").isLatest(false).withType(type))
      .build();


    SearchResult searchResult = instance.execute(graph, new SearchRequestV2_1());

    assertThat(searchResult.getFacets(), is(Matchers.notNullValue()));

    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(facetDescription1, times(1)).getFacet(captor.capture());
    assertThat((List<Vertex>) captor.getValue(), contains(likeVertex().withType(type)));
    ArgumentCaptor<List> captor1 = ArgumentCaptor.forClass(List.class);
    verify(facetDescription2, times(1)).getFacet(captor1.capture());
    assertThat((List<Vertex>) captor1.getValue(), contains(likeVertex().withType(type)));
  }


  private AbstractSearchDescriptionBuilder searchDescription() {
    return new AbstractSearchDescriptionBuilder();
  }

  static class DefaultSearchDescription extends AbstractSearchDescription {
    private final List<String> sortableFields;

    private final List<String> fullTextSearchFields;
    private final String type;
    private final PropertyDescriptor idDescriptor;
    private final List<FacetDescription> facetDescriptions;
    private final Map<String, PropertyDescriptor> dataPropertyDescriptors;
    private final PropertyDescriptor displayNameDescriptor;

    public DefaultSearchDescription(PropertyDescriptor idDescriptor, PropertyDescriptor displayNameDescriptor,
                                    List<FacetDescription> facetDescriptions,
                                    Map<String, PropertyDescriptor> dataPropertyDescriptors,
                                    List<String> sortableFields, List<String> fullTextSearchFields,
                                    String type) {
      this.facetDescriptions = facetDescriptions;
      this.dataPropertyDescriptors = dataPropertyDescriptors;
      this.displayNameDescriptor = displayNameDescriptor;
      this.idDescriptor = idDescriptor;
      this.sortableFields = sortableFields;
      this.fullTextSearchFields = fullTextSearchFields;
      this.type = type;
    }

    @Override
    public List<String> getSortableFields() {
      return sortableFields;
    }

    @Override
    public List<String> getFullTextSearchFields() {
      return fullTextSearchFields;
    }

    @Override
    protected List<FacetDescription> getFacetDescriptions() {
      return facetDescriptions;
    }

    @Override
    protected Map<String, PropertyDescriptor> getDataPropertyDescriptors() {
      return dataPropertyDescriptors;
    }

    @Override
    protected PropertyDescriptor getDisplayNameDescriptor() {
      return displayNameDescriptor;
    }

    @Override
    protected PropertyDescriptor getIdDescriptor() {
      return idDescriptor;
    }

    @Override
    protected String getType() {
      return type;
    }

  }

  private static class AbstractSearchDescriptionBuilder {

    private PropertyDescriptor idDescriptor;
    private PropertyDescriptor displayNameDescriptor;
    private List<FacetDescription> facetDescriptions;
    private Map<String, PropertyDescriptor> dataPropertyDescriptions;
    private List<String> sortableFields;
    private List<String> fullTextSearchFields;
    private String type;

    private AbstractSearchDescriptionBuilder() {
      idDescriptor = vertex -> "";
      displayNameDescriptor = vertex -> "";
      facetDescriptions = Lists.newArrayList();
      dataPropertyDescriptions = Maps.newHashMap();
      sortableFields = Lists.newArrayList();
      fullTextSearchFields = Lists.newArrayList();
      type = null;
    }

    public AbstractSearchDescription build() {
      return new DefaultSearchDescription(idDescriptor, displayNameDescriptor, facetDescriptions,
        dataPropertyDescriptions, sortableFields, fullTextSearchFields, type);
    }

    public AbstractSearchDescriptionBuilder withFacetDescription(FacetDescription facetDescription) {
      facetDescriptions.add(facetDescription);
      return this;
    }

    public AbstractSearchDescriptionBuilder withIdDescriptor(PropertyDescriptor idDescriptor) {
      this.idDescriptor = idDescriptor;
      return this;
    }

    public AbstractSearchDescriptionBuilder withDisplayNameDescriptor(PropertyDescriptor displayNameDescriptor) {
      this.displayNameDescriptor = displayNameDescriptor;
      return this;
    }

    private AbstractSearchDescriptionBuilder withDataDescriptor(String propertyName,
                                                                PropertyDescriptor dataDescriptor) {
      this.dataPropertyDescriptions.put(propertyName, dataDescriptor);
      return this;
    }

    private AbstractSearchDescriptionBuilder withType(String type) {
      this.type = type;

      return this;
    }
  }
}
