package nl.knaw.huygens.timbuctoo.model.properties.converters;

public class Converters {
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


  public static StringArrayToEncodedArrayOfLimitedValues stringArrayToEncodedArrayOf(String... values) {
    return new StringArrayToEncodedArrayOfLimitedValues(values);
  }

  public static StringToEncodedStringOfLimitedValuesConverter stringToEncodedStringOf(String... values) {
    return new StringToEncodedStringOfLimitedValuesConverter(values);
  }

  public static StringToUnencodedStringOfLimitedValuesConverter stringToUnencodedStringOf(String... values) {
    return new StringToUnencodedStringOfLimitedValuesConverter(values);
  }

}
