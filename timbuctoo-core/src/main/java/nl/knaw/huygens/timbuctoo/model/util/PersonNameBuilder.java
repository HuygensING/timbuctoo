package nl.knaw.huygens.timbuctoo.model.util;

import java.util.Map;
import java.util.regex.Pattern;

import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Supports building of a string representation of a name.
 */
public class PersonNameBuilder {

  private static final String EMPTY = "";
  private static final String SPACE = " ";
  private static final String COMMA = ", ";

  private static final Map<MultiKey, String> separators = createSeparatorMap();

  private static Map<MultiKey, String> createSeparatorMap() {
    Map<MultiKey, String> map = Maps.newHashMap();
    map.put(new MultiKey(Type.SURNAME, Type.FORENAME), COMMA);
    map.put(new MultiKey(Type.SURNAME, Type.GEN_NAME), COMMA);
    map.put(new MultiKey(Type.SURNAME, Type.ADD_NAME), COMMA);
    map.put(new MultiKey(Type.SURNAME, Type.NAME_LINK), COMMA);
    map.put(new MultiKey(Type.FORENAME, Type.ADD_NAME), COMMA);
    map.put(new MultiKey(Type.GEN_NAME, Type.ADD_NAME), COMMA);
    return map;
  }

  @VisibleForTesting
  static String separator(Type type1, Type type2) {
    if (type1 == null || type2 == null) {
      return EMPTY;
    } else {
      String value = separators.get(new MultiKey(type1, type2));
      return (value != null) ? value : SPACE;
    }
  }

  // -------------------------------------------------------------------

  private static final Pattern ELISIONS = Pattern.compile("\\b([dDlL]')\\s+");

  private final StringBuilder builder;
  private Type prev;

  public PersonNameBuilder() {
    builder = new StringBuilder();
    prev = null;
  }

  public void addComponent(PersonNameComponent component) {
    Type type = component.getType();
    builder.append(separator(prev, type));
    builder.append(component.getValue());
    prev = type;
  }

  public String getName() {
    return ELISIONS.matcher(builder).replaceAll("$1");
  }

  // -------------------------------------------------------------------

  /**
   * Combines two type values to form a single, composite key.
   */
  private static class MultiKey {
    private final Type key1;
    private final Type key2;

    public MultiKey(Type key1, Type key2) {
      this.key1 = Preconditions.checkNotNull(key1);
      this.key2 = Preconditions.checkNotNull(key2);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MultiKey) {
        MultiKey that = (MultiKey) obj;
        return this.key1 == that.key1 && this.key2 == that.key2;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 31 * key1.hashCode() + key2.hashCode();
    }
  }

}
