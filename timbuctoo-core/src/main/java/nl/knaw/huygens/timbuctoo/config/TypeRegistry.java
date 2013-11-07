package nl.knaw.huygens.timbuctoo.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.annotations.DoNotRegister;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

/**
 * The type registry contains properties of entity classes.
 *
 * We distinguish two types of entities:<ul>
 * <li>System entities are for internal use in the repository;
 * they are not versioned and do not have variations.</li>
 * <li>Domain entities are used for modeling user entities;
 * they are versioned and may have variations.</li>
 * </ul>
 *
 * <p>The type registry scans specified Java packages for concrete
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

  private final Map<String, Set<Class<? extends DomainEntity>>> variationMap = Maps.newHashMap();

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
        registerVariationForClass((Class<? extends Entity>) type);
        LOG.debug("Registered {}", type.getName());
      }
    }
  }

  private boolean shouldRegisterClass(Class<?> type) {
    return isEntity(type) //
        && !Modifier.isAbstract(type.getModifiers()) //
        && !type.isAnnotationPresent(DoNotRegister.class);
  }

  // -------------------------------------------------------------------

  private <T extends Entity> void registerClass(Class<T> type) {
    String iname = TypeNameGenerator.getInternalName(type);
    if (iname2type.containsKey(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    iname2type.put(iname, type);
    type2iname.put(type, iname);

    String xname = TypeNameGenerator.getExternalName(type);
    if (xname2type.containsKey(xname)) {
      throw new IllegalStateException("Duplicate internal type name " + xname);
    }
    xname2type.put(xname, type);
    type2xname.put(type, xname);

    iname2xname.put(iname, xname);
  }

  private void registerVariationForClass(Class<? extends Entity> type) {
    if (DomainEntity.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked")
      Class<? extends DomainEntity> domainEntity = (Class<? extends DomainEntity>) type;
      String variation = getClassVariation(domainEntity);

      if (variation != null) {
        if (variationMap.containsKey(variation)) {
          variationMap.get(variation).add(domainEntity);
        } else {
          Set<Class<? extends DomainEntity>> set = Sets.newHashSet();
          set.add(domainEntity);
          variationMap.put(variation, set);
        }
      }
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

  /**
   * Gets a sub class of the primitive that correspondents with the {@code variation}.
   * @param typeForVariation should be a class of the model package.
   * @param variation should be a sub-package of the model package. 
   * @return the class if one is found, null if not.
   */
  public Class<? extends DomainEntity> getVariationClass(Class<? extends DomainEntity> typeForVariation, String variation) {
    if (!variationMap.containsKey(variation)) {
      return null;
    }

    for (Class<? extends DomainEntity> domainEntity : variationMap.get(variation)) {
      if (typeForVariation.isAssignableFrom(domainEntity)) {
        return domainEntity;
      }
    }

    return null;
  }

  /**
   * Determines the variation of a class. This is based on the package the class is placed in.
   * @param type the type the variation should be determined of.
   * @return the variation. This will be null for each primitive (i.e. Person) and supporting classes (like DomainEntity).
   */
  public String getClassVariation(Class<? extends DomainEntity> type) {
    String packageName = type.getPackage().getName();
    if (packageName.endsWith(".model")) {
      return null;
    }

    return packageName.substring(packageName.lastIndexOf('.') + 1);
  }

  // --- static utilities ----------------------------------------------

  public static boolean isEntity(Class<?> cls) {
    return Entity.class.isAssignableFrom(cls);
  }

  public static boolean isSystemEntity(Class<?> cls) {
    return SystemEntity.class.isAssignableFrom(cls);
  }

  public static boolean isDomainEntity(Class<?> cls) {
    return DomainEntity.class.isAssignableFrom(cls);
  }

  /**
   * Forces the typecast of the specified class to an entity type token.
   */
  public static <T extends Entity> Class<T> toEntity(Class<?> cls) throws ClassCastException {
    if (isEntity(cls)) {
      @SuppressWarnings("unchecked")
      Class<T> result = (Class<T>) cls;
      return result;
    }
    throw new ClassCastException(cls.getName() + " is not an entity");
  }

  /**
   * Forces the typecast of the specified class to a system entity type token.
   */
  public static <T extends SystemEntity> Class<T> toSystemEntity(Class<?> cls) throws ClassCastException {
    if (isSystemEntity(cls)) {
      @SuppressWarnings("unchecked")
      Class<T> result = (Class<T>) cls;
      return result;
    }
    throw new ClassCastException(cls.getName() + " is not a system entity");
  }

  /**
   * Forces the typecast of the specified class to a domain entity type token.
   */
  public static <T extends DomainEntity> Class<T> toDomainEntity(Class<?> cls) throws ClassCastException {
    if (isDomainEntity(cls)) {
      @SuppressWarnings("unchecked")
      Class<T> result = (Class<T>) cls;
      return result;
    }
    throw new ClassCastException(cls.getName() + " is not a domain entity");
  }

}
