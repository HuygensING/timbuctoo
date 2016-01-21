package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.commons.lang.StringUtils;

public class GenderPropParser implements PropParser {
  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    return StringUtils.strip(value, "\"");
  }
}
