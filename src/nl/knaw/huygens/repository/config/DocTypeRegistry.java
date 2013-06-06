package nl.knaw.huygens.repository.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.annotations.DoNotRegister;
import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

@Singleton
public class DocTypeRegistry {

  private final Logger LOG = LoggerFactory.getLogger(DocTypeRegistry.class);

  private final ClassPath classPath;
  private final Map<String, Class<? extends Document>> webServiceTypeStringToTypeMap;
  private final Map<Class<? extends Document>, String> typeToStringMap;
  private final Map<Class<? extends Document>, String> typeToCollectionIdMap;

  public DocTypeRegistry(String packageNames) {
    try {
      classPath = ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

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

  // FIXME all inits should be done at construction time!
  public String getTypeString(Class<? extends Document> type) {
    if (typeToStringMap.containsKey(type)) {
      return typeToStringMap.get(type);
    }
    return getCollectionName(type);
  }

  public Class<? extends Document> getClassFromWebServiceTypeString(String typeString) {
    return webServiceTypeStringToTypeMap.get(typeString);
  }

  // FIXME all inits should be done at construction time!
  public String getCollectionId(Class<? extends Document> type) {
    if (typeToCollectionIdMap.containsKey(type)) {
      return typeToCollectionIdMap.get(type);
    }
    String collectionId = getCollectionName(getBaseClass(type));
    typeToCollectionIdMap.put(type, collectionId);
    return collectionId;
  }

  @SuppressWarnings("unchecked")
  private void registerPackage(String packageName) {
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      Class<?> cls = info.load();
      if (shouldRegisterClass(cls)) {
        registerClass(packageName, (Class<? extends Document>) cls);
      }
    }
  }

  private boolean shouldRegisterClass(Class<?> cls) {
    return Document.class.isAssignableFrom(cls) //
        && !Modifier.isAbstract(cls.getModifiers()) //
        && !cls.isAnnotationPresent(DoNotRegister.class);
  }

  private void registerClass(String packageName, Class<? extends Document> type) {
    String typeId = determineTypeName(type);
    webServiceTypeStringToTypeMap.put(typeId, type);
    typeToStringMap.put(type, typeId);
    Class<? extends Document> baseCls = getBaseClass(type);
    String baseTypeId = getCollectionName(baseCls);
    typeToCollectionIdMap.put(type, baseTypeId);
    LOG.info("Identified '{}' in package {}", typeId, packageName);
  }

  /**
   * Returns all registered document types.
   */
  public Set<Class<? extends Document>> getDocumentTypes() {
    return Collections.unmodifiableSet(typeToStringMap.keySet());
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
