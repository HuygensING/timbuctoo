package nl.knaw.huygens.repository.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
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
 * a {@code DoNotRegister} annotation on a class.</p>
 *
 * <p>The use of classes as type tokens is connected to type erasure
 * of Java generics. In addition to type tokens we use two string
 * representations of document types:
 * - internal names, which are used, for instance, for indicating
 * document types in JSON (so as to hide implementation details),
 * for Solr core names, and for Mongo collection names.
 * - external names, which are used in the REST API for indicating
 * document collections.
 * Internal names are simply detailed a rule: the lower case form
 * of the simple class name (the last part of the fully qualified
 * class name). External names are determined as the plural of the
 * internal name (constructed by appending an 's' to the internal
 * name) or a name supplied in a class annotation.</p>
 */
@Singleton
public class DocTypeRegistry {

  private final Logger LOG = LoggerFactory.getLogger(DocTypeRegistry.class);

  private final Map<Class<? extends Document>, String> type2iname = Maps.newHashMap();
  private final Map<String, Class<? extends Document>> iname2type = Maps.newHashMap();

  private final Map<Class<? extends Document>, String> type2xname = Maps.newHashMap();
  private final Map<String, Class<? extends Document>> xname2type = Maps.newHashMap();

  private final Map<String, String> iname2xname = Maps.newHashMap();

  public DocTypeRegistry(String packageNames) {
    Preconditions.checkNotNull(packageNames, "'packageNames' must not be null");

    ClassPath classPath = getClassPath();
    for (String packageName : StringUtils.split(packageNames)) {
      registerPackage(classPath, packageName);
    }
  }

  private ClassPath getClassPath() {
    try {
      return ClassPath.from(this.getClass().getClassLoader());
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
    String iname = getInternalName(type);
    if (iname2type.containsKey(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    iname2type.put(iname, type);
    type2iname.put(type, iname);

    String xname = getExternalName(type);
    if (xname2type.containsKey(xname)) {
      throw new IllegalStateException("Duplicate internal type name " + xname);
    }
    xname2type.put(xname, type);
    type2xname.put(type, xname);

    iname2xname.put(iname, xname);
  }

  private String getInternalName(Class<? extends Document> type) {
    return type.getSimpleName().toLowerCase();
  }

  private String getExternalName(Class<? extends Document> type) {
    if (type.isAnnotationPresent(DocumentTypeName.class)) {
      return type.getAnnotation(DocumentTypeName.class).value();
    } else {
      return getInternalName(type) + "s";
    }
  }

  // --- public api ----------------------------------------------------

  /**
   * Returns the internal type names.
   */
  public Set<String> getTypeStrings() {
    return ImmutableSortedSet.copyOf(iname2type.keySet());
  }

  /**
   * Returns the internal type name for the specified type token,
   * or {@code null} if there is no such name.
   */
  public String getINameForType(Class<? extends Document> type) {
    return type2iname.get(type);
  }

  /**
   * Returns the type token for the specified internal type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Document> getTypeForIName(String iname) {
    return iname2type.get(iname);
  }

  /**
   * Returns the external type name for the specified type token,
   * or {@code null} if there is no such name.
   */
  public String getXNameForType(Class<? extends Document> type) {
    return type2xname.get(type);
  }

  /**
   * Returns the type token for the specified external type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Document> getTypeForXName(String xname) {
    return xname2type.get(xname);
  }

  /**
   * Returns the external type name for the specified internal type name,
   * or {@code null} if there is no such name.
   */
  public String getXNameForIName(String iname) {
    return iname2xname.get(iname);
  }

  @SuppressWarnings("unchecked")
  public Class<? extends Document> getBaseClass(Class<? extends Document> type) {
    Class<? extends Document> lastType = type;
    while (type != null && !Modifier.isAbstract(type.getModifiers())) {
      lastType = type;
      type = (Class<? extends Document>) type.getSuperclass();
    }
    return lastType;
  }

}
