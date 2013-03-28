package nl.knaw.huygens.repository.model.util;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DocumentTypeRegister {

  private final ClassPath classPath;
  private final Map<String, Class<? extends Document>> stringToTypeMap;
  private final Map<Class<? extends Document>, String> typeToStringMap;
  private final Map<Class<? extends Document>, String> typeToCollectionIdMap;
  private final List<String> unreadablePackages;

  public DocumentTypeRegister() {
    this(null);
  }

  @Inject
  public DocumentTypeRegister(@Named("model-packages") String packageNames) {
    try {
      classPath = ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    stringToTypeMap = Maps.newHashMap();
    typeToStringMap = Maps.newHashMap();
    typeToCollectionIdMap = Maps.newHashMap();
    unreadablePackages = Lists.newArrayList();
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
    return ImmutableSortedSet.copyOf(stringToTypeMap.keySet());
  }

  public String getTypeString(Class<? extends Document> type) {
    if (typeToStringMap.containsKey(type)) {
      return typeToStringMap.get(type);
    }
    return getCollectionName(type);
  }

  public Class<? extends Document> getClassFromTypeString(String id) {
    // NB: in the DB, package names will be prefixed to class names with a dash
    // (-) suffix.
    // These need to be removed in order to find the classes again:
    String normalizedId = id.replaceFirst("[a-z]*-", "");
    if (stringToTypeMap.containsKey(normalizedId)) {
      return stringToTypeMap.get(normalizedId);
    }
    String className = StringUtils.capitalize(normalizedId);
    for (String packageName : unreadablePackages) {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends Document> cls = (Class<? extends Document>) Class.forName(packageName + "." + className);
        return cls;
      } catch (Exception ex) {

      }
    }
    return null;
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
  public void registerPackage(String packageId) {
    int classesDetected = 0;
    for (ClassInfo info : classPath.getTopLevelClasses(packageId)) {
      Class<?> cls = info.load();
      if (isDocumentType(cls)) {
        Class<? extends Document> docCls = (Class<? extends Document>) cls;
        String typeId = docCls.getSimpleName().toLowerCase();
        stringToTypeMap.put(typeId, docCls);
        typeToStringMap.put(docCls, typeId);
        Class<? extends Document> baseCls = getBaseClass(docCls);
        String baseTypeId = getCollectionName(baseCls);
        typeToCollectionIdMap.put(docCls, baseTypeId);
        System.out.printf("Identified '%s' in package %s%n", typeId, packageId);
        classesDetected++;
      }
    }
    if (classesDetected == 0) {
      System.out.printf("Package %s: no types - adding package for runtime checking%n", packageId);
      unreadablePackages.add(packageId);
    }
  }

  private boolean isDocumentType(Class<?> type) {
    return Document.class.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers());
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Document> getBaseClass(Class<? extends Document> type) {
    Class<? extends Document> lastType = type;
    while (type != null && !type.equals(Document.class)) {
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

}
