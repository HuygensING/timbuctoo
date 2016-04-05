package nl.knaw.huygens.timbuctoo.search.description;


import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ReceptionSearchDescriptionTest {
  private FacetDescriptionFactory facetDescriptionFactory;
  private PropertyDescriptorFactory propertyDescriptorFactory;

  @Before
  public void setUp() throws Exception {
    PropertyParserFactory propertyParserFactory = new PropertyParserFactory();
    facetDescriptionFactory = new FacetDescriptionFactory(propertyParserFactory);
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
  }

  @Test
  public void testCreateRef() {
    SearchResult searchResult = mock(SearchResult.class);
    SearchDescription searchDescription = mock(SearchDescription.class);
    EntityRef otherRef = new EntityRef("wwdocument", "id");

    Map<String, Object> otherData = Maps.newHashMap();
    otherData.put("prop", "val");
    otherRef.setData(otherData);

    given(searchResult.getSearchDescription()).willReturn(searchDescription);
    given(searchDescription.getType()).willReturn("wwdocument");

    ReceptionSearchDescription instance = new ReceptionSearchDescription(propertyDescriptorFactory,
            facetDescriptionFactory, searchResult);

    List<Vertex> vertices = newGraph()
            .withVertex("v1", v -> v.withTimId("id1").withProperty("wwdocument_title", "title"))
            .withVertex("v2", v -> v.withTimId("id2").withOutgoingRelation("isWorkCommentedOnIn", "v1"))
            .build().traversal().V().toList();

    given(searchDescription.createRef(vertices.get(1))).willReturn(otherRef);


    EntityRef ref = instance.createRef(vertices.get(0));

    assertThat(ref.getSourceData(), equalTo(otherRef.getData()));
    assertThat(ref.getTargetData().get("_id"), equalTo("id1"));
    assertThat(ref.getTargetData().get("title"), equalTo("title"));
  }
}
