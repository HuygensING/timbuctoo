package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class PropertyDescriptorFactoryTest {

  private PropertyDescriptorFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PropertyDescriptorFactory();
  }

  @Test
  public void getLocalWithPropertyNameAndParserReturnsALocalPropertyDescriptor() {
    PropertyParser parser = mock(PropertyParser.class);

    PropertyDescriptor descriptor = instance.getLocal("propertyName", parser);

    assertThat(descriptor, is(instanceOf(LocalPropertyDescriptor.class)));
  }

  @Test
  public void getDerivedReturnsADerivedPropertyDescriptor() {
    PropertyParser parser = mock(PropertyParser.class);

    PropertyDescriptor descriptor = instance.getDerived("relationName", "propertyName", parser);

    assertThat(descriptor, is(instanceOf(DerivedPropertyDescriptor.class)));
  }

  @Test
  public void getDerivedWithSeparatorReturnsADerivedPropertyDescriptor() {
    PropertyParser parser = mock(PropertyParser.class);

    PropertyDescriptor descriptor = instance.getDerivedWithSeparator("relationName", "propertyName", parser, "--");

    assertThat(descriptor, is(instanceOf(DerivedPropertyDescriptor.class)));
  }


  @Test
  public void getCompositeReturnsACompositePropertyDescriptor() {
    PropertyDescriptor descriptor =
      instance.getComposite(mock(PropertyDescriptor.class), mock(PropertyDescriptor.class));

    assertThat(descriptor, is(instanceOf(CompositePropertyDescriptor.class)));
  }

}
