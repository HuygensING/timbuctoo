package nl.knaw.huygens.repository.index;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

/**
 * Contains various index-related utility methods.
 */
public class Utils {

  /**
   * Determines the index field name from the method name (only used if the
   * annotation doesn't specify a fieldname).
   * 
   * @param method
   *          the Method object for which a Solr field name should be generated.
   * @return the field name
   */
  public static String getFieldName(Method method) {
    String name = method.getName();
    String type = method.getReturnType().getSimpleName();
    String rv = name.startsWith("get") ? name.substring(3) : name; // eliminate
                                                                   // 'get' part
    String[] parts = StringUtils.splitByCharacterTypeCamelCase(rv);
    type = type.replaceAll("\\[\\]", "");
    if (type.equals("boolean")) {
      type = "b";
    } else if (type.equals("int") || type.equals("long")) {
      type = "i";
    } else {
      type = "s";
    }
    return "facet_" + type + "_" + StringUtils.join(parts, "_").toLowerCase();
  }

  private Utils() {
    throw new AssertionError("Non-instantiable class");
  }

}
