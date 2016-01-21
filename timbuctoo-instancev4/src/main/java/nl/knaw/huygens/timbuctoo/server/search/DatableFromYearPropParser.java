package nl.knaw.huygens.timbuctoo.server.search;

public class DatableFromYearPropParser implements PropParser {

  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    Datable datable = new Datable(value);

    return datable.isValid() ? String.valueOf(datable.getFromYear()) : null;
  }
}
