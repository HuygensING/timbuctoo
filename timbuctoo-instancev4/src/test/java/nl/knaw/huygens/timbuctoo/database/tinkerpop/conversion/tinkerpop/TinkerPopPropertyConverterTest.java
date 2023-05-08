package nl.knaw.huygens.timbuctoo.database.tinkerpop.conversion.tinkerpop;


import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DatableProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.core.UnknownPropertyException;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.conversion.TinkerPopPropertyConverter;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TinkerPopPropertyConverterTest {

  public static final String PROPERTY_NAME = "name";
  public static final String STRING_VALUE = "";

  @Test
  public void fromCreatesAStringPropertyWhenUsesTheUniqueTypeIdOfThePropertyToDetermineTheTimPropertyType()
    throws UnknownPropertyException, IOException {
    Collection collection = mock(Collection.class);
    ReadableProperty readableProperty = mock(ReadableProperty.class);
    when(readableProperty.getUniqueTypeId()).thenReturn("string");
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    TimProperty property = instance.from(PROPERTY_NAME, STRING_VALUE);

    assertThat(property, is(instanceOf(StringProperty.class)));
  }

  @Test
  public void fromThrowsAnUnknownPropertyExceptionWhenThePropertyDoesNotExistInTheCollection()
    throws UnknownPropertyException, IOException {
    Assertions.assertThrows(UnknownPropertyException.class, () -> {
      Collection collection = mock(Collection.class);
      when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.empty());
      TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

      instance.from(PROPERTY_NAME, STRING_VALUE);
    });
  }

  @Test
  public void fromThrowsAnUnknownPropertyExceptionWhenThePropertyHasAnUnknownUniqueTypeId()
    throws UnknownPropertyException, IOException {
    Assertions.assertThrows(UnknownPropertyException.class, () -> {
      Collection collection = mock(Collection.class);
      ReadableProperty readableProperty = mock(ReadableProperty.class);
      when(readableProperty.getUniqueTypeId()).thenReturn("unknownType");
      when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
      TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

      instance.from(PROPERTY_NAME, STRING_VALUE);
    });
  }

  @Test
  public void fromThrowsAnIoExceptionIfThePropertyCannotBeConverted()
    throws UnknownPropertyException, IOException {
    Assertions.assertThrows(IOException.class, () -> {
      Collection collection = mock(Collection.class);
      ReadableProperty readableProperty = mock(ReadableProperty.class);
      when(readableProperty.getUniqueTypeId()).thenReturn("string");
      when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
      TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

      instance.from(PROPERTY_NAME, new Object());
    });
  }

  @Test
  public void toReturnsTheNameOfAnArrayOfLimitedValuesProperty() throws Exception {
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(null);
    ArrayOfLimitedValuesProperty property = new ArrayOfLimitedValuesProperty("name", "[]");

    Tuple<String, Object> value = instance.to(property);

    assertThat(value.getLeft(), is("name"));
  }

  // Datable tests
  @Test
  public void fromReturnsADatablePropertyWithADecodedStringValue() throws Exception {
    Collection collection = mock(Collection.class);
    ReadableProperty readableProperty = mock(ReadableProperty.class);
    when(readableProperty.getUniqueTypeId()).thenReturn("datable");
    when(collection.getProperty(PROPERTY_NAME)).thenReturn(Optional.of(readableProperty));
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(collection);

    TimProperty<?> from = instance.from(PROPERTY_NAME, "\"1800\"");

    assertThat(from.getValue(), is("1800"));
  }

  @Test
  public void toReturnsAJsonEncodedStringForADatableProperty() throws Exception {
    TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(null);
    DatableProperty property = new DatableProperty(PROPERTY_NAME, "1800");

    Tuple<String, Object> value = instance.to(property);

    assertThat(value.getRight(), is("\"1800\""));
  }

  @Test
  public void toThrowsAnExceptionWhenTheDateFormatIsNotSupported() throws Exception {
    Assertions.assertThrows(IOException.class, () -> {
      TinkerPopPropertyConverter instance = new TinkerPopPropertyConverter(null);
      DatableProperty property = new DatableProperty(PROPERTY_NAME, "01-02-180");

      instance.to(property);
    });
  }
}
