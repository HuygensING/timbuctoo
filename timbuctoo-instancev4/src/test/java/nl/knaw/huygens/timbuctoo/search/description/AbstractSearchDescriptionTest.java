package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortDescription;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.FullTextSearchParameter;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SearchRequestV2_1;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SortParameter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription
  .createLocalSimpleFullTextSearchDescription;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AbstractSearchDescriptionTest {

  @Test
  public void executeCreatesASearchResult() {
    AbstractSearchDescription instance = searchDescription().build();
    Graph graph = newGraph().build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);

    SearchResult searchResult = instance.execute(graphWrapper, new SearchRequestV2_1());

    assertThat(searchResult, is(Matchers.notNullValue()));
  }

  private GraphWrapper createGraphWrapper(Graph graph) {
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graph);
    given(graphWrapper.getLatestState()).willReturn(graph.traversal());
    return graphWrapper;
  }

  @Test
  public void executeCreatesASearchResultWithTheSortableAndFullTextSearchFieldsOfTheDescription() {
    String sortableField1 = "sortableField1";
    String sortableField2 = "sortableField2";
    String searchField1 = "searchField1";
    String searchField2 = "searchField2";
    AbstractSearchDescription instance = searchDescription()
      .withSortableFields(sortableField1, sortableField2)
      .withFullTextSearchFields(searchField1, searchField2)
      .build();
    Graph graph = newGraph().build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);

    SearchResult searchResult = instance.execute(graphWrapper, new SearchRequestV2_1());

    assertThat(searchResult.getSortableFields(), containsInAnyOrder(sortableField1, sortableField2));
    assertThat(searchResult.getFullTextSearchFields(), containsInAnyOrder(searchField1, searchField2));
  }

  // TODO extract ref creator class
  @Test
  public void executeCreatesARefForEachVertexWithTheRightType() {
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
      .withVertex(vertex -> vertex.withTimId("id").withType(type))
      .withVertex(vertex -> vertex.withTimId("id1").withType("otherType"))
      .build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);

    SearchResult searchResult = instance.execute(graphWrapper, new SearchRequestV2_1());

    assertThat(searchResult.getRefs(), is(not(empty())));

    verify(idDescriptor, times(1)).get(argThat(likeVertex().withType(type)));
    verify(displayNameDescriptor, times(1)).get(argThat(likeVertex().withType(type)));
    verify(dataDescriptor1, times(1)).get(argThat(likeVertex().withType(type)));
    verify(dataDescriptor2, times(1)).get(argThat(likeVertex().withType(type)));
  }

  @Test
  public void executeCreatesAnEmptyListOfRefsIfNoVerticesMatchTheRequest() {
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
      .withFullTextSearchDescription(createLocalSimpleFullTextSearchDescription("ftsd", "prop"))
      .build();
    Graph graph = newGraph()
      .withVertex(vertex -> vertex.withTimId("id").withType(type))
      .withVertex(vertex -> vertex.withTimId("id1").withType("otherType"))
      .build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);

    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    searchRequest.setFullTextSearchParameters(Lists.newArrayList(new FullTextSearchParameter("ftsd", "test")));
    SearchResult searchResult = instance.execute(graphWrapper, searchRequest);

    assertThat(searchResult.getRefs(), is(empty()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeLetsEachFacetDescriptionFillAListOfFacets() {
    FacetDescription facetDescription1 = mock(FacetDescription.class);
    FacetDescription facetDescription2 = mock(FacetDescription.class);
    String type = "type";
    AbstractSearchDescription instance = searchDescription()
      .withType(type)
      .withFacetDescription(facetDescription1)
      .withFacetDescription(facetDescription2)
      .build();
    Graph graph = newGraph()
      .withVertex(vertex -> vertex.withTimId("id").withType(type))
      .withVertex(vertex -> vertex.withTimId("id1").withType("otherType"))
      .build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);

    SearchResult searchResult = instance.execute(graphWrapper, new SearchRequestV2_1());

    assertThat(searchResult.getFacets(), is(Matchers.notNullValue()));
    ArgumentCaptor<GraphTraversal> captor = ArgumentCaptor.forClass(GraphTraversal.class);
    verify(facetDescription1, times(1)).getFacet(captor.capture());

    assertThat(((GraphTraversal<Vertex, Vertex>) captor.getValue()).toList(), contains(likeVertex().withType(type)));
    ArgumentCaptor<GraphTraversal> captor1 = ArgumentCaptor.forClass(GraphTraversal.class);
    verify(facetDescription2, times(1)).getFacet(captor1.capture());
    assertThat(((GraphTraversal<Vertex, Vertex>) captor1.getValue()).toList(), contains(likeVertex().withType(type)));
  }

  @Test
  public void executeLetsEachFacetDescriptorFilterTheSearchResult() {
    FacetDescription facetDescription1 = mock(FacetDescription.class);
    FacetDescription facetDescription2 = mock(FacetDescription.class);
    AbstractSearchDescription instance = searchDescription()
      .withFacetDescription(facetDescription1)
      .withFacetDescription(facetDescription2)
      .build();
    Graph graph = newGraph().build();
    GraphWrapper graphWrapper = createGraphWrapper(graph);
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();

    instance.execute(graphWrapper, searchRequest);

    verify(facetDescription1).filter(any(/*GraphTraversal*/), argThat(is(searchRequest.getFacetValues())));
    verify(facetDescription2).filter(any(/*GraphTraversal*/), argThat(is(searchRequest.getFacetValues())));
  }
  // TODO add tests to make sure the filtering happens before the creation of the facets and the results.

  @Test
  public void executeLetsEachFullTextDescriptionFilterTheSearchResult() {
    GraphWrapper graphWrapper = createGraphWrapper(newGraph().build());
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    List<FullTextSearchParameter> fullTextSearchParameters = Lists.newArrayList(
      new FullTextSearchParameter("name1", "value1"),
      new FullTextSearchParameter("name2", "value2"));
    searchRequest.setFullTextSearchParameters(fullTextSearchParameters);
    FullTextSearchDescription fullTextSearchDescription1 = fullTextSearchDescriptionWithName("name1");
    FullTextSearchDescription fullTextSearchDescription2 = fullTextSearchDescriptionWithName("name2");
    AbstractSearchDescription instance = searchDescription()
      .withFullTextSearchDescription(fullTextSearchDescription1)
      .withFullTextSearchDescription(fullTextSearchDescription2)
      .build();

    instance.execute(graphWrapper, searchRequest);

    verify(fullTextSearchDescription1).filter(any(), argThat(hasProperty("name", equalTo("name1"))));
    verify(fullTextSearchDescription2).filter(any(), argThat(hasProperty("name", equalTo("name2"))));
  }

  @Test
  public void executeIgnoreTheFullTextSearchParametersThatDoNotHaveADescriptions() {
    GraphWrapper graphWrapper = createGraphWrapper(newGraph().build());
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    List<FullTextSearchParameter> fullTextSearchParameters = Lists.newArrayList(
      new FullTextSearchParameter("name1", "value1"),
      new FullTextSearchParameter("name3", "value3"));
    searchRequest.setFullTextSearchParameters(fullTextSearchParameters);
    FullTextSearchDescription fullTextSearchDescription1 = fullTextSearchDescriptionWithName("name1");
    AbstractSearchDescription instance = searchDescription()
      .withFullTextSearchDescription(fullTextSearchDescription1)
      .build();

    instance.execute(graphWrapper, searchRequest);

    verify(fullTextSearchDescription1).filter(any(), argThat(hasProperty("name", equalTo("name1"))));
  }

  @Test
  public void executeDoesNotFilterWithFullTextSearchDescriptionsThatHaveNoParameter() {
    GraphWrapper graphWrapper = createGraphWrapper(newGraph().build());
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    List<FullTextSearchParameter> fullTextSearchParameters = Lists.newArrayList(
      new FullTextSearchParameter("name1", "value1"));
    searchRequest.setFullTextSearchParameters(fullTextSearchParameters);
    FullTextSearchDescription fullTextSearchDescription1 = fullTextSearchDescriptionWithName("name1");
    FullTextSearchDescription fullTextSearchDescription2 = fullTextSearchDescriptionWithName("name2");
    AbstractSearchDescription instance = searchDescription()
      .withFullTextSearchDescription(fullTextSearchDescription1)
      .withFullTextSearchDescription(fullTextSearchDescription2)
      .build();

    instance.execute(graphWrapper, searchRequest);

    verify(fullTextSearchDescription1).filter(any(), argThat(hasProperty("name", equalTo("name1"))));
    verifyZeroInteractions(fullTextSearchDescription2);
  }

  @Test
  public void executeLetsEachSortDescriptionSort() {
    GraphWrapper graphWrapper = createGraphWrapper(newGraph().build());
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    ArrayList<SortParameter> sortParameters = Lists.newArrayList(
      new SortParameter("field", SortParameter.Direction.asc));
    searchRequest.setSortParameters(sortParameters);
    SortDescription sortDescription = mock(SortDescription.class);
    AbstractSearchDescription instance = searchDescription().withSortDescription(sortDescription).build();

    instance.execute(graphWrapper, searchRequest);

    verify(sortDescription).sort(any(), argThat(is(sortParameters)));
  }

  private FullTextSearchDescription fullTextSearchDescriptionWithName(String name) {
    FullTextSearchDescription fullTextSearchDescription1 = mock(FullTextSearchDescription.class);
    given(fullTextSearchDescription1.getName()).willReturn(name);
    return fullTextSearchDescription1;
  }

  private AbstractSearchDescriptionBuilder searchDescription() {
    return new AbstractSearchDescriptionBuilder();
  }

  static class DefaultSearchDescription extends AbstractSearchDescription {
    private final List<String> sortableFields;

    private final List<String> fullTextSearchFields;
    private final SortDescription sortDescription;
    private final String type;
    private final PropertyDescriptor idDescriptor;
    private final List<FacetDescription> facetDescriptions;
    private final Map<String, PropertyDescriptor> dataPropertyDescriptors;
    private final PropertyDescriptor displayNameDescriptor;
    private List<FullTextSearchDescription> fullTextSearchDescriptions;

    public DefaultSearchDescription(PropertyDescriptor idDescriptor, PropertyDescriptor displayNameDescriptor,
                                    List<FacetDescription> facetDescriptions,
                                    Map<String, PropertyDescriptor> dataPropertyDescriptors,
                                    List<String> sortableFields, List<String> fullTextSearchFields,
                                    List<FullTextSearchDescription> fullTextSearchDescriptions,
                                    SortDescription sortDescription, String type) {
      this.facetDescriptions = facetDescriptions;
      this.dataPropertyDescriptors = dataPropertyDescriptors;
      this.displayNameDescriptor = displayNameDescriptor;
      this.idDescriptor = idDescriptor;
      this.sortableFields = sortableFields;
      this.fullTextSearchFields = fullTextSearchFields;
      this.fullTextSearchDescriptions = fullTextSearchDescriptions;
      this.sortDescription = sortDescription;
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

    @Override
    public List<FullTextSearchDescription> getFullTextSearchDescriptions() {
      return fullTextSearchDescriptions;
    }

    @Override
    protected SortDescription getSortDescription() {
      return sortDescription;
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
    private List<FullTextSearchDescription> fullTextSearchDescriptions;
    private SortDescription sortDescription;

    private AbstractSearchDescriptionBuilder() {
      idDescriptor = vertex -> "";
      displayNameDescriptor = vertex -> "";
      facetDescriptions = Lists.newArrayList();
      dataPropertyDescriptions = Maps.newHashMap();
      sortableFields = Lists.newArrayList();
      fullTextSearchFields = Lists.newArrayList();
      fullTextSearchDescriptions = Lists.newArrayList();
      sortDescription = new SortDescription(Lists.newArrayList());
      type = null;
    }

    public AbstractSearchDescription build() {
      return new DefaultSearchDescription(idDescriptor, displayNameDescriptor, facetDescriptions,
        dataPropertyDescriptions, sortableFields, fullTextSearchFields, fullTextSearchDescriptions, sortDescription,
        type);
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

    private AbstractSearchDescriptionBuilder withSortableFields(String... sortableFields) {
      this.sortableFields.addAll(Arrays.asList(sortableFields));
      return this;
    }

    private AbstractSearchDescriptionBuilder withFullTextSearchFields(String... fullTextSearchFields) {
      this.fullTextSearchFields.addAll(Arrays.asList(fullTextSearchFields));
      return this;
    }

    private AbstractSearchDescriptionBuilder withFullTextSearchDescription(FullTextSearchDescription description) {
      this.fullTextSearchDescriptions.add(description);
      return this;
    }

    public AbstractSearchDescriptionBuilder withSortDescription(SortDescription sortDescription) {
      this.sortDescription = sortDescription;
      return this;
    }
  }
}
