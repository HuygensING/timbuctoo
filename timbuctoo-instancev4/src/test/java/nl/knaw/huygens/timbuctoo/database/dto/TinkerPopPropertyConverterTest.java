package nl.knaw.huygens.timbuctoo.database.dto;


import nl.knaw.huygens.timbuctoo.database.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TinkerPopPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.dto.property.UnknownPropertyException;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TinkerPopPropertyConverterTest {

  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_VALUE = "";

  @Test
  public void fromCreatesAStringPropertyWhenUsesTheUniqueTypeIdOfThePropertyToDetermineTheTimPropertyType()
    throws UnknownPropertyException, IOException {
    Collection collection = mock(Collection.class);
    ReadableProperty readableProperty = mock(ReadableProperty.class);
    when(readableProperty.getUniqueTypeId()).thenReturn("string");
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    TimProperty property = instance.from(PROPERTY_NAME, PROPERTY_VALUE);

    assertThat(property, is(instanceOf(StringProperty.class)));
  }

  @Test(expected = UnknownPropertyException.class)
  public void fromThrowsAnUnknownPropertyExceptionWhenThePropertyDoesNotExistInTheCollection()
    throws UnknownPropertyException, IOException {
    Collection collection = mock(Collection.class);
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.empty());
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    instance.from(PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test(expected = UnknownPropertyException.class)
  public void fromThrowsAnUnknownPropertyExceptionWhenThePropertyHasAnUnknownUniqueTypeId()
    throws UnknownPropertyException, IOException {
    Collection collection = mock(Collection.class);
    ReadableProperty readableProperty = mock(ReadableProperty.class);
    when(readableProperty.getUniqueTypeId()).thenReturn("unknownType");
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    instance.from(PROPERTY_NAME, PROPERTY_VALUE);
  }

  @Test(expected = IOException.class)
  public void fromThrowsAnIoExceptionIfThePropertyCannotBeConverted()
    throws UnknownPropertyException, IOException {
    Collection collection = mock(Collection.class);
    ReadableProperty readableProperty = mock(ReadableProperty.class);
    when(readableProperty.getUniqueTypeId()).thenReturn("string");
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    instance.from(PROPERTY_NAME, new Object());
  }

}
