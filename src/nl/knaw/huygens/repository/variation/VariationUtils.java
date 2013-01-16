package nl.knaw.huygens.repository.variation;

public class VariationUtils {
  public static final String AGREED = "a";
  public static final String VALUE = "v";
  public static final String COMMON_PROPS = "common";
  public static final String BASE_MODEL_PACKAGE_VARIATION = "model";

  public static String getVariationName(Class<?> cls) {
    String packageName = cls.getPackage().getName();
    final String variationName = packageName.substring(packageName.lastIndexOf('.') + 1);
    return variationName;
  }

  public static Class<?> getBaseClass(Class<?> cls) {
    Class<?> lastCls = null;
    while (cls != null) {
      lastCls = cls;
      cls = cls.getSuperclass();
    }
    return lastCls;
  }

  public static Class<?> getEarliestCommonClass(Class<?> cls) {
    while (cls != null) {
      if (getVariationName(cls).equals(BASE_MODEL_PACKAGE_VARIATION)) {
        return cls;
      }
      cls = cls.getSuperclass();
    }
    return null;
  }
}
