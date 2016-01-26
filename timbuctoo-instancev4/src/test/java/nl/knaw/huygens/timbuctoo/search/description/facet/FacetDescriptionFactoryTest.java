package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class FacetDescriptionFactoryTest {
  @Test
  public void createListFacetDescriptionCreatesAListFacetDescription() {
    FacetDescriptionFactory instance = new FacetDescriptionFactory();
    PropertyParser parser = mock(PropertyParser.class);

    FacetDescription listFacetDescription = instance.createListFacetDescription("facetName", "propertyName", parser);

    assertThat(listFacetDescription, is(instanceOf(ListFacetDescription.class)));
  }
}
