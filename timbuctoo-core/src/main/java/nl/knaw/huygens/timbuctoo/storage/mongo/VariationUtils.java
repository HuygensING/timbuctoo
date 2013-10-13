package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Modifier;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Lists;

public class VariationUtils {

  public static final String AGREED = "a";
  public static final String VALUE = "v";
  public static final String BASE_MODEL_PACKAGE_VARIATION = "model";
  public static final String DEFAULT_VARIATION = "!defaultVRE";

  public static String getVariationName(Class<?> cls) {
    String packageName = cls.getPackage().getName();
    return packageName.substring(packageName.lastIndexOf('.') + 1);
  }

  @SuppressWarnings("unchecked")
  public static List<Class<? extends Entity>> getAllClasses(Class<? extends Entity> cls) {
    List<Class<? extends Entity>> rv = Lists.newArrayList();
    Class<? extends Entity> myCls = cls;
    while (myCls != null && !Modifier.isAbstract(myCls.getModifiers())) {
      rv.add(myCls);
      myCls = (Class<? extends Entity>) myCls.getSuperclass();
    }
    return rv;
  }

  // Conversion between (domain) model type tokens and variation names.
  // The conversion rules are used by VariationReducer and VariationInducer only.

  public static String typeToVariationName(Class<?> cls) {
    String clsId = cls.getSimpleName().toLowerCase();
    String variationName = getVariationName(cls);
    if (variationName.equals(BASE_MODEL_PACKAGE_VARIATION)) {
      return clsId;
    }
    return variationName + "-" + clsId;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Entity> Class<? extends T> variationNameToType(DocTypeRegistry registry, String id) {
    return (Class<? extends T>) registry.getTypeForIName(normalize(id));
  }

  private static String normalize(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
  }

}
