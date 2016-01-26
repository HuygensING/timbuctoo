package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class SearchDescriptionFactoryTest {

  private SearchDescriptionFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new SearchDescriptionFactory(mock(FacetDescriptionFactory.class),
      mock(PropertyDescriptorFactory.class));
  }

  @Test
  public void createCreatesADefaultSearchDescriptionForTheStringWwPerson() {
    SearchDescription searchDescription = instance.create("wwperson");

    assertThat(searchDescription, is(instanceOf(DefaultSearchDescription.class)));
  }

  @Test
  public void createCreatesAWwDocumentSearchDescriptionForTheStringWwDocument() {
    SearchDescription searchDescription = instance.create("wwdocument");

    assertThat(searchDescription, is(instanceOf(WwDocumentSearchDescription.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createThrowsAnIllegalArgumentExceptionForAnUnknownType() {
    instance.create("unknownType");
  }
}

