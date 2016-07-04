package nl.knaw.huygens.timbuctoo.model.properties.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Converters {
  private static final Map<String, Class<? extends Converter>> CONVERTER_TYPES = new HashMap<>();

  static {
    CONVERTER_TYPES.put("person-names", PersonNamesConverter.class);
    CONVERTER_TYPES.put("encoded-array-of-limited-values", StringArrayToEncodedArrayOfLimitedValues.class);
    CONVERTER_TYPES.put("encoded-string-of-limited-values", StringToEncodedStringOfLimitedValuesConverter.class);
    CONVERTER_TYPES.put("datable", DatableConverter.class);
    CONVERTER_TYPES.put("string", StringToStringConverter.class);
    CONVERTER_TYPES.put("hyperlinks", HyperlinksConverter.class);
    CONVERTER_TYPES.put("default-person-display-name", DefaultFullPersonNameConverter.class);
    CONVERTER_TYPES.put("unencoded-string-of-limited-values", StringToUnencodedStringOfLimitedValuesConverter.class);
    CONVERTER_TYPES.put("encoded-array", ArrayToEncodedArrayConverter.class);
    CONVERTER_TYPES.put("altnames", AltNamesConverter.class);
  }

  public static final StringToStringConverter stringToString = new StringToStringConverter();
  public static final ArrayToEncodedArrayConverter arrayToEncodedArray = new ArrayToEncodedArrayConverter();

  public static final HyperlinksConverter hyperlinks = new HyperlinksConverter();
  public static final DatableConverter datable = new DatableConverter();
  public static final StringToEncodedStringOfLimitedValuesConverter gender = stringToEncodedStringOf(
    "UNKNOWN",
    "MALE",
    "FEMALE",
    "NOT_APPLICABLE"
  );
  public static final PersonNamesConverter personNames = new PersonNamesConverter();
  public static final StringToUnencodedStringOfLimitedValuesConverter stringOfYesNoUnknown = stringToUnencodedStringOf(
    "YES",
    "NO",
    "UNKNOWN"
  );

  //Converters that have a more specific usecase
  public static final DefaultLocationNameConverter defaultLocationNameConverter = new DefaultLocationNameConverter();
  public static final DefaultFullPersonNameConverter defaultFullPersonNameConverter
    = new DefaultFullPersonNameConverter();
  // CNW AltNames converter
  public static final AltNamesConverter altNames = new AltNamesConverter();


  public static StringArrayToEncodedArrayOfLimitedValues stringArrayToEncodedArrayOf(String... values) {
    return new StringArrayToEncodedArrayOfLimitedValues(values);
  }

  public static StringToEncodedStringOfLimitedValuesConverter stringToEncodedStringOf(String... values) {
    return new StringToEncodedStringOfLimitedValuesConverter(values);
  }

  public static StringToUnencodedStringOfLimitedValuesConverter stringToUnencodedStringOf(String... values) {
    return new StringToUnencodedStringOfLimitedValuesConverter(values);
  }

  public static Converter forType(String type, String[] options)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    final Class<? extends Converter> converterClass = CONVERTER_TYPES.get(type);

    if (converterClass.isAssignableFrom(HasOptions.class)) {
      final Constructor<? extends Converter> hasOptionsConstuctor =
        converterClass.getDeclaredConstructor(String[].class);
      return hasOptionsConstuctor.newInstance((Object[]) options);
    } else {
      return converterClass.newInstance();
    }
  }
}
