package nl.knaw.huygens.repository.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.annotations.DoNotRegister;
import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.StringUtils;
import org.scribe.utils.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

/**
 * The document registry contains properties of document classes.
 * 
 * In principle those properties can be retrieved from the classes
 * by reflection, but a number of them are cached for quick access.
 * 
 * We distinguish two types of documents:<ul>
 * <li>System documents are for internal use in the repository;
 * they are not versioned and do not have variations.</li>
 * <li>Domain documents are used for modeling user entities;
 * they are versioned and may have variations.</li>
 * </ul>
 *
 * <p>The document registry scans specified Java packages for concrete
 * (i.e. not abstract) classes that subclass {@code Document}.
 * The developer has the option to prevent registration by providing
 * a {@code DoNotRegister} annotation.</p>
 *
 * <p>To distinguish between document types we use two characterizations:
 * - type token (usually abbreviated to type), which is the Java
 * {@code Class<? extends Document>} for the document
 * - type name (usually abbreviated to name) which is a Java string.
 * There is a one-to-one correspondence between the type token and the
 * type name. Hence type names must be unique.
 * The default rule for constructing type names is to take the lower
 * case version of the last part of the fully qualified class name.
 * This rule can be overridden by using a {@code DocumentTypeName}
 * annotation on the document class.</p>
 */
@Singleton
public class DocTypeRegistry {

  private final Logger LOG = LoggerFactory.getLogger(DocTypeRegistry.class);

  private final Map<String, Class<? extends Document>> webServiceTypeStringToTypeMap;
  private final Map<Class<? extends Document>, String> typeToStringMap;
  private final Map<Class<? extends Document>, String> typeToCollectionIdMap;

  private Map<Class<? extends Document>, String> type2name = Maps.newHashMap();
  private Map<String, Class<? extends Document>> name2type = Maps.newHashMap();

  public DocTypeRegistry(String packageNames) {
    Preconditions.checkNotNull(packageNames, "packageNames must not be null");

    webServiceTypeStringToTypeMap = Maps.newHashMap();
    typeToStringMap = Maps.newHashMap();
    typeToCollectionIdMap = Maps.newHashMap();

    ClassPath classPath = getClassPath();
    for (String packageName : StringUtils.split(packageNames)) {
      registerPackage(classPath, packageName);
    }
  }

  private ClassPath getClassPath() {
    try {
      return ClassPath.from(getClass().getClassLoader());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private void registerPackage(ClassPath classPath, String packageName) {
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      Class<?> type = info.load();
      if (shouldRegisterClass(type)) {
        registerClass((Class<? extends Document>) type);
        LOG.info("Registered {}", type.getName());
      }
    }
  }

  private boolean shouldRegisterClass(Class<?> type) {
    return Document.class.isAssignableFrom(type) //
        && !Modifier.isAbstract(type.getModifiers()) //
        && !type.isAnnotationPresent(DoNotRegister.class);
  }

  // -------------------------------------------------------------------

  private void registerClass(Class<? extends Document> type) {
    String name = getTypeName(type);
    if (name2type.containsKey(name)) {
      throw new RuntimeException("Duplicate document type name " + name);
    }
    name2type.put(name, type);
    type2name.put(type, name);

    // old stuff

    String typeId = determineTypeName(type);
    webServiceTypeStringToTypeMap.put(typeId, type);
    typeToStringMap.put(type, typeId);
    Class<? extends Document> baseCls = getBaseClass(type);
    String baseTypeId = getCollectionName(baseCls);
    typeToCollectionIdMap.put(type, baseTypeId);
  }

  private String getTypeName(Class<? extends Document> type) {
    if (type.isAnnotationPresent(DocumentTypeName.class)) {
      return type.getAnnotation(DocumentTypeName.class).value();
    } else {
      return type.getSimpleName().toLowerCase();
    }
  }

  /**
   * Returns the document type name for the specified type token,
   * or {@code null} if there is no such name.
   */
  public String docNameFor(Class<? extends Document> type) {
    return type2name.get(type);
  }

  /**
   * Returns the document type token for the specified type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Document> docTypeFor(String name) {
    return name2type.get(name);
  }

  // -------------------------------------------------------------------

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

  private static String determineTypeName(Class<? extends Document> type) {
    DocumentTypeName annotation = type.getAnnotation(DocumentTypeName.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return type.getSimpleName().toLowerCase() + "s";
    }
  }

}
