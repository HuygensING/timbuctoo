package nl.knaw.huygens.timbuctoo.server.search.property;

import nl.knaw.huygens.timbuctoo.server.search.PropDescriptor;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class PropertyDescriptorFactoryTest {

  @Test
  public void getLocalWithPropertyNameAndParserReturnsALocalPropertyDesciptor() {
    PropertyDescriptorFactory instance = new PropertyDescriptorFactory();
    String propertyName = "propertyName";
    PropertyParser parser = mock(PropertyParser.class);

    PropDescriptor descriptor = instance.getLocal(propertyName, parser);

    assertThat(descriptor, is(instanceOf(LocalPropertyDescriptor.class)));
  }


}
