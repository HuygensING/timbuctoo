package nl.knaw.huygens.repository.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

@Singleton
public class DocTypeRegistry {

  private final ClassPath classPath;
  private final Map<String, Class<? extends Document>> mongoTypeStringToTypeMap;
  private final Map<String, Class<? extends Document>> webServiceTypeStringToTypeMap;
  private final Map<Class<? extends Document>, String> typeToStringMap;
  private final Map<Class<? extends Document>, String> typeToCollectionIdMap;

  public DocTypeRegistry(String packageNames) {
    try {
      classPath = ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    mongoTypeStringToTypeMap = Maps.newHashMap();
    webServiceTypeStringToTypeMap = Maps.newHashMap();
    typeToStringMap = Maps.newHashMap();
    typeToCollectionIdMap = Maps.newHashMap();
    if (packageNames != null) {
      for (String packageName : StringUtils.split(packageNames)) {
        registerPackage(packageName);
      }
    }
  }

  /**
   * Returns the registered document types.
   */
  public Set<String> getTypeStrings() {
    return ImmutableSortedSet.copyOf(webServiceTypeStringToTypeMap.keySet());
  }

  public String getTypeString(Class<? extends Document> type) {
    if (typeToStringMap.containsKey(type)) {
      return typeToStringMap.get(type);
    }
    return getCollectionName(type);
  }

  public Class<? extends Document> getClassFromWebServiceTypeString(String typeString) {
    // NB: in the DB, package names will be prefixed to class names with a dash (-) suffix.
    // These need to be removed in order to find the classes again:
    String normalizedTypeString = normalizeTypeString(typeString);
    return webServiceTypeStringToTypeMap.get(normalizedTypeString);
  }

  private String normalizeTypeString(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
  }

  public Class<? extends Document> getClassFromMongoTypeString(String typeString) {
    String normalizedTypeString = normalizeTypeString(typeString);
    return mongoTypeStringToTypeMap.get(normalizedTypeString);
  }

  public String getCollectionId(Class<? extends Document> type) {
    if (typeToCollectionIdMap.containsKey(type)) {
      return typeToCollectionIdMap.get(type);
    }
    String collectionId = getCollectionName(getBaseClass(type));
    typeToCollectionIdMap.put(type, collectionId);
    return collectionId;
  }

  @SuppressWarnings("unchecked")
  private void registerPackage(String packageId) {
    int classesDetected = 0;
    for (ClassInfo info : classPath.getTopLevelClasses(packageId)) {
      Class<?> cls = info.load();
      if (isDocumentType(cls)) {
        Class<? extends Document> docCls = (Class<? extends Document>) cls;
        String typeId = determineTypeName(docCls);
        webServiceTypeStringToTypeMap.put(typeId, docCls);
        mongoTypeStringToTypeMap.put(docCls.getSimpleName().toLowerCase(), docCls);
        typeToStringMap.put(docCls, typeId);
        Class<? extends Document> baseCls = getBaseClass(docCls);
        String baseTypeId = getCollectionName(baseCls);
        typeToCollectionIdMap.put(docCls, baseTypeId);
        System.out.printf("Identified '%s' in package %s%n", typeId, packageId);
        classesDetected++;
      }
    }
    if (classesDetected == 0) {
      System.out.printf("Package %s: no types%n", packageId);
    }
  }

  /**
   * Returns all registered document types.
   */
  public Set<Class<? extends Document>> getDocumentTypes() {
    return Collections.unmodifiableSet(typeToStringMap.keySet());
  }

  private boolean isDocumentType(Class<?> type) {
    return Document.class.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers());
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Document> getBaseClass(Class<? extends Document> type) {
    Class<? extends Document> lastType = type;
    while (type != null && !Modifier.isAbstract(type.getModifiers())) {
      lastType = type;
      type = (Class<? extends Document>) type.getSuperclass();
    }
    return lastType;
  }

  public static String getVersioningCollectionName(Class<? extends Document> type) {
    return getCollectionName(type) + "-versions";
  }

  public static String getCollectionName(Class<? extends Document> type) {
    return type.getSimpleName().toLowerCase();
  }

  public static String determineTypeName(Class<? extends Document> type) {
    DocumentTypeName annotation = type.getAnnotation(DocumentTypeName.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return type.getSimpleName().toLowerCase() + "s";
    }
  }

}
