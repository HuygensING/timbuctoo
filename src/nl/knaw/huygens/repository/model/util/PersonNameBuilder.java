package nl.knaw.huygens.repository.model.util;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.keyvalue.MultiKey;

import com.google.common.collect.Maps;

/**
 * Supports building of a string representation of a name.
 */
public class PersonNameBuilder {

  private static final String EMPTY = "";
  private static final String SPACE = " ";
  private static final String COMMA = ", ";

  private static final Map<MultiKey, String> eparators = createSeparatorMap();

  private static Map<MultiKey, String> createSeparatorMap() {
    Map<MultiKey, String> map = Maps.newHashMap();
    map.put(new MultiKey(PersonNameComponentType.SURNAME, PersonNameComponentType.FORENAME), COMMA);
    map.put(new MultiKey(PersonNameComponentType.SURNAME, PersonNameComponentType.GEN_NAME), COMMA);
    map.put(new MultiKey(PersonNameComponentType.SURNAME, PersonNameComponentType.ADD_NAME), COMMA);
    map.put(new MultiKey(PersonNameComponentType.SURNAME, PersonNameComponentType.NAME_LINK), COMMA);
    map.put(new MultiKey(PersonNameComponentType.FORENAME, PersonNameComponentType.ADD_NAME), COMMA);
    map.put(new MultiKey(PersonNameComponentType.GEN_NAME, PersonNameComponentType.ADD_NAME), COMMA);
    return map;
  }

  public static String separator(PersonNameComponentType type1, PersonNameComponentType type2) {
    if (type1 == null || type2 == null) {
      return EMPTY;
    } else {
      String value = eparators.get(new MultiKey(type1, type2));
      return (value != null) ? value : SPACE;
    }
  }

  // -------------------------------------------------------------------

  private static final Pattern ELISIONS = Pattern.compile("\\b([dDlL]')\\s+");

  private final StringBuilder builder;
  private PersonNameComponentType prev;

  public PersonNameBuilder() {
    builder = new StringBuilder();
    prev = null;
  }

  public void addComponent(PersonNameComponent component) {
    PersonNameComponentType type = component.getType();
    builder.append(separator(prev, type));
    builder.append(component.getValue());
    prev = type;
  }

  public String getName() {
    return ELISIONS.matcher(builder).replaceAll("$1");
  }

}
