package nl.knaw.huygens.timbuctoo.server.search.propertygetter;

import nl.knaw.huygens.timbuctoo.server.search.PropertyGetter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PropertyGetterFactoryTest {
  @Test
  public void getLocalReturnsALocalPropGetter() {
    PropertyGetterFactory instance = new PropertyGetterFactory();

    PropertyGetter propertyGetter = instance.getLocal("fieldName");

    assertThat(propertyGetter, is(instanceOf(LocalPropertyGetter.class)));
  }
}
