package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultSearchDescriptionTest {

  @Test
  public void createRefCreatesARefCallingEachDataPropertyAndTheIdAndDisplayNameDescriptors() {
    PropertyDescriptor idDescriptor = mock(PropertyDescriptor.class);
    PropertyDescriptor displayNameDescriptor = mock(PropertyDescriptor.class);
    PropertyDescriptor dataDescriptor1 = mock(PropertyDescriptor.class);
    PropertyDescriptor dataDescriptor2 = mock(PropertyDescriptor.class);
    Map<String, PropertyDescriptor> dataDescriptors = Maps.newHashMap();
    dataDescriptors.put("desc1", dataDescriptor1);
    dataDescriptors.put("desc2", dataDescriptor2);
    DefaultSearchDescription instance =
      new DefaultSearchDescription(idDescriptor, displayNameDescriptor, null, dataDescriptors, null, null, null);
    Vertex vertex = vertex().build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref, is(notNullValue()));

    verify(idDescriptor).get(vertex);
    verify(displayNameDescriptor).get(vertex);
    verify(dataDescriptor1).get(vertex);
    verify(dataDescriptor2).get(vertex);
  }

  @Test
  public void createFacetsLetsEachFacetDescriptionFillAListOfFacets(){
    FacetDescription facetDescription1 = mock(FacetDescription.class);
    FacetDescription facetDescription2 = mock(FacetDescription.class);
    List<FacetDescription> facetDescriptions = Lists.newArrayList(facetDescription1, facetDescription2);
    DefaultSearchDescription instance =
      new DefaultSearchDescription(null, null, facetDescriptions, null, null, null, null);
    List<Vertex> vertices = Lists.newArrayList(vertex().build());

    List<Facet> facets = instance.createFacets(vertices);

    assertThat(facets, is(Matchers.notNullValue()));

    verify(facetDescription1).getFacet(vertices);
    verify(facetDescription2).getFacet(vertices);
  }
}
