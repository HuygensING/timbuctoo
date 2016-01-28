package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
    Optional<SearchDescription> searchDescription = instance.create("wwperson");

    assertThat(searchDescription.get(), is(instanceOf(DefaultSearchDescription.class)));
  }

  @Test
  public void createCreatesAWwDocumentSearchDescriptionForTheStringWwDocument() {
    Optional<SearchDescription> searchDescription = instance.create("wwdocument");

    assertThat(searchDescription.get(), is(instanceOf(WwDocumentSearchDescription.class)));
  }

  @Test
  public void createReturnsAnEmtptyOptionalForAnUnknownType() {
    Optional<SearchDescription> searchDescription = instance.create("unknownType");

    assertThat(searchDescription, is(not(present())));
  }
}

