package nl.knaw.huygens.repository.variation;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

import com.google.common.collect.Lists;

public class VariationUtils {
  public static final String AGREED = "a";
  public static final String VALUE = "v";
  public static final String BASE_MODEL_PACKAGE_VARIATION = "model";

  public static String getVariationName(Class<?> cls) {
    String packageName = cls.getPackage().getName();
    return packageName.substring(packageName.lastIndexOf('.') + 1);
  }

  public static <T extends Document> Class<? extends Document> getBaseClass(Class<? extends T> cls) {
    List<Class<? extends Document>> allClasses = getAllClasses(cls);
    return allClasses.get(allClasses.size() - 1);
  }

  @SuppressWarnings("unchecked")
  public static List<Class<? extends Document>> getAllClasses(Class<? extends Document> cls) {
    List<Class<? extends Document>> rv = Lists.newArrayList();
    Class<? extends Document> myCls = cls;
    while (myCls != null && !myCls.equals(Document.class)) {
      rv.add(myCls);
      myCls = (Class<? extends Document>) myCls.getSuperclass();
    }
    return rv;
  }

  public static String getClassId(Class<?> cls) {
    String clsId = cls.getSimpleName().toLowerCase();
    String variationName = getVariationName(cls);
    if (variationName.equals(BASE_MODEL_PACKAGE_VARIATION)) {
      return clsId;
    }
    return variationName + "-" + clsId;
  }
}
