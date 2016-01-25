package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SearchDescriptionFactoryTest {
  @Test
  public void createCreatesAWwPersonSearchDescriptionForTheStringWwPerson() {
    SearchDescriptionFactory instance = new SearchDescriptionFactory();

    SearchDescription searchDescription = instance.create("wwperson");

    assertThat(searchDescription, is(instanceOf(WwPersonSearchDescription.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createThrowsAnIllegalArgumentExceptionForAnUnknownType() {
    SearchDescriptionFactory instance = new SearchDescriptionFactory();

    instance.create("unknownType");
  }

}
