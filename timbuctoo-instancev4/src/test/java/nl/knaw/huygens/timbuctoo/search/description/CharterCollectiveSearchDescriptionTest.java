package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertexWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CharterCollectiveSearchDescriptionTest {

  private CharterCollectiveSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    PropertyParserFactory propertyParserFactory = new PropertyParserFactory();
    FacetDescriptionFactory facetDescriptionFactory = new FacetDescriptionFactory(propertyParserFactory);
    PropertyDescriptorFactory propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
    instance = new CharterCollectiveSearchDescription(propertyDescriptorFactory, facetDescriptionFactory);
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";

    Vertex vertex = vertexWithId(id).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

}
