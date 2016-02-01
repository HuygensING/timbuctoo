package nl.knaw.huygens.timbuctoo.model.converters;

public class Converters {
  public static final Converter stringToString = new StringToStringConverter();
  public static final Converter arrayToEncodedArray = new ArrayToEncodedArrayConverter();
}
