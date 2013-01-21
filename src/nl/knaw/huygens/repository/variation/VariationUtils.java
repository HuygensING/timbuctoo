package nl.knaw.huygens.repository.variation;

import nl.knaw.huygens.repository.model.Document;

public class VariationUtils {
  public static final String AGREED = "a";
  public static final String VALUE = "v";
  public static final String COMMON_PROPS = "common";
  public static final String BASE_MODEL_PACKAGE_VARIATION = "model";

  public static String getVariationName(Class<? extends Document> cls) {
    String packageName = cls.getPackage().getName();
    final String variationName = packageName.substring(packageName.lastIndexOf('.') + 1);
    return variationName;
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends Document> getBaseClass(Class<? extends Document> cls) {
    Class<? extends Document> lastCls = cls;
    while (cls != null && !cls.equals(Document.class)) {
      lastCls = cls;
      cls = (Class<? extends Document>) cls.getSuperclass();
    }
    return lastCls;
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends Document> getFirstCommonClass(Class<? extends Document> cls) {
    while (cls != null && !cls.equals(Document.class)) {
      if (getVariationName(cls).equals(BASE_MODEL_PACKAGE_VARIATION)) {
        return cls;
      }
      cls = (Class<? extends Document>) cls.getSuperclass();
    }
    return null;
  }
}
