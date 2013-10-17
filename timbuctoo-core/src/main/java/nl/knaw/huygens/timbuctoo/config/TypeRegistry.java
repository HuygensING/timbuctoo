package nl.knaw.huygens.timbuctoo.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.annotations.DoNotRegister;
import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

/**
 * The document registry contains properties of entity classes.
 *
 * We distinguish two types of entities:<ul>
 * <li>System entities are for internal use in the repository;
 * they are not versioned and do not have variations.</li>
 * <li>Domain entities are used for modeling user entities;
 * they are versioned and may have variations.</li>
 * </ul>
 *
 * <p>The document registry scans specified Java packages for concrete
 * (i.e. not abstract) classes that subclass {@code Entity}.
 * The developer has the option to prevent registration by providing
 * a {@code DoNotRegister} annotation on a class.</p>
 *
 * <p>The use of classes as type tokens is connected to type erasure
 * of Java generics. In addition to type tokens we use two string
 * representations of entity types:
 * - internal names, which are used, for instance, for indicating
 * entity types in JSON (so as to hide implementation details),
 * for Solr core names, and for Mongo collection names.
 * - external names, which are used in the REST API for indicating
 * entity collections.
 * Internal names are simply detailed a rule: the lower case form
 * of the simple class name (the last part of the fully qualified
 * class name). External names are determined as the plural of the
 * internal name (constructed by appending an 's' to the internal
 * name) or a name supplied in a class annotation.</p>
 */
@Singleton
public class TypeRegistry {

  private final Logger LOG = LoggerFactory.getLogger(TypeRegistry.class);

  private final Map<Class<? extends Entity>, String> type2iname = Maps.newHashMap();
  private final Map<String, Class<? extends Entity>> iname2type = Maps.newHashMap();

  private final Map<Class<? extends Entity>, String> type2xname = Maps.newHashMap();
  private final Map<String, Class<? extends Entity>> xname2type = Maps.newHashMap();

  private final Map<String, String> iname2xname = Maps.newHashMap();

  public TypeRegistry(String packageNames) {
    checkArgument(packageNames != null, "'packageNames' must not be null");

    ClassPath classPath = getClassPath();
    for (String packageName : StringUtils.split(packageNames)) {
      registerPackage(classPath, packageName.replaceFirst("^timbuctoo", "nl.knaw.huygens.timbuctoo"));
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
        registerClass((Class<? extends Entity>) type);
        LOG.info("Registered {}", type.getName());
      }
    }
  }

  private boolean shouldRegisterClass(Class<?> type) {
    return Entity.class.isAssignableFrom(type) //
        && !Modifier.isAbstract(type.getModifiers()) //
        && !type.isAnnotationPresent(DoNotRegister.class);
  }

  // -------------------------------------------------------------------

  private void registerClass(Class<? extends Entity> type) {
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

  private String getInternalName(Class<? extends Entity> type) {
    return type.getSimpleName().toLowerCase();
  }

  private String getExternalName(Class<? extends Entity> type) {
    if (type.isAnnotationPresent(EntityTypeName.class)) {
      return type.getAnnotation(EntityTypeName.class).value();
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
  public String getINameForType(Class<? extends Entity> type) {
    return type2iname.get(type);
  }

  /**
   * Returns the type token for the specified internal type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Entity> getTypeForIName(String iname) {
    return iname2type.get(iname);
  }

  /**
   * Returns the external type name for the specified type token,
   * or {@code null} if there is no such name.
   */
  public String getXNameForType(Class<? extends Entity> type) {
    return type2xname.get(type);
  }

  /**
   * Returns the type token for the specified external type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Entity> getTypeForXName(String xname) {
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
  public Class<? extends Entity> getBaseClass(Class<? extends Entity> type) {
    Class<? extends Entity> lastType = type;
    while (type != null && !Modifier.isAbstract(type.getModifiers())) {
      lastType = type;
      type = (Class<? extends Entity>) type.getSuperclass();
    }
    return lastType;
  }

}
