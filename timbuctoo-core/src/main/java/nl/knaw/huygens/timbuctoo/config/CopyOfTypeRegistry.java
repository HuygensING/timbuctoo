package nl.knaw.huygens.timbuctoo.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.annotations.DoNotRegister;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.Variable;

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
public class CopyOfTypeRegistry {

  private final Logger LOG = LoggerFactory.getLogger(CopyOfTypeRegistry.class);

  private final Map<Class<? extends Entity>, String> type2iname = Maps.newHashMap();
  private final Map<String, Class<? extends Entity>> iname2type = Maps.newHashMap();

  private final Map<Class<? extends Entity>, Set<Class<? extends Entity>>> subClassMap = Maps.newHashMap();

  private final Map<String, Set<Class<? extends Variable>>> variationMap = Maps.newHashMap();

  private final Map<Class<? extends Entity>, String> type2xname = Maps.newHashMap();
  private final Map<String, Class<? extends Entity>> xname2type = Maps.newHashMap();

  private final Map<String, String> iname2xname = Maps.newHashMap();

  private final Map<String, Class<? extends Role>> iname2role = Maps.newHashMap();
  private final Map<Class<? extends Role>, String> role2iname = Maps.newHashMap();

  public CopyOfTypeRegistry(String packageNames) {
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

  private void registerPackage(ClassPath classPath, String packageName) {
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      Class<?> type = info.load();
      if (shouldRegisterEntity(type)) {
        if (isValidSystemEntity(type)) {
          registerClass(toEntity(type));
        } else if (isValidDomainEntity(type)) {
          registerClass(toEntity(type));
          registerVariationForClass(toDomainEntity(type));
          registerWithBaseClass(toDomainEntity(type));
        } else {
          LOG.error("Not a valid entity: '{}'", type.getName());
          throw new IllegalStateException("Invalid entity");
        }
        LOG.debug("Registered entity {}", type.getName());
      } else if (shouldRegisterRole(type)) {
        registerRole(toRole(type));
        registerVariationForClass(toRole(type));
      }
    }
  }

  private void registerWithBaseClass(Class<? extends Entity> type) {
    Class<? extends Entity> baseClass = getBaseClass(type);

    if (subClassMap.containsKey(baseClass)) {
      subClassMap.get(baseClass).add(type);
    } else {
      Set<Class<? extends Entity>> subClasses = Sets.newHashSet();
      subClasses.add(type);
      subClassMap.put(baseClass, subClasses);
    }
  }

  private boolean shouldRegisterRole(Class<?> type) {
    return isRole(type) && !shouldNotRegister(type);
  }

  private boolean shouldRegisterEntity(Class<?> type) {
    return isEntity(type) && !shouldNotRegister(type);
  }

  private boolean shouldNotRegister(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) //
        || type.isAnnotationPresent(DoNotRegister.class);
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

  private <T extends Role> void registerRole(Class<T> role) {
    String iname = TypeNameGenerator.getInternalName(role);
    if (iname2role.containsKey(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    iname2role.put(iname, role);
    role2iname.put(role, iname);
  }

  private void registerVariationForClass(Class<? extends Variable> type) {
    String variation = getClassVariation(type);

    if (variation != null) {
      if (variationMap.containsKey(variation)) {
        variationMap.get(variation).add(type);
      } else {
        Set<Class<? extends Variable>> set = Sets.newHashSet();
        set.add(type);
        variationMap.put(variation, set);
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
   * Returns the internal type name for the specified role type token,
   * or {@code null} if there is no such name.
   */
  public String getINameForRole(Class<? extends Role> role) {
    return role2iname.get(role);
  }

  /**
   * Convenience method that returns {@code getINameForType} or {@code getINameForRole} or null depending on the parameter. 
   * @param type the type to get the internal name from. 
   * @return
   */
  @SuppressWarnings("unchecked")
  public String getIName(Class<?> type) {
    if (isEntity(type)) {
      return getINameForType((Class<? extends Entity>) type);
    } else if (isRole(type)) {
      return getINameForRole(toRole(type));
    }
    return null;
  }

  /**
   * Returns the type token for the specified internal type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Entity> getTypeForIName(String iName) {
    return iname2type.get(iName);
  }

  /**
   * Returns the role type token for the specified internal name
   * or {@code null} if there is no such token.
   * @param iName the internal name that is requested.
   * @return
   */
  public Class<? extends Role> getRoleForIName(String iName) {
    return iname2role.get(iName);
  }

  /**
   * Returns a {@code Role} class or a {@code Entity} class if one is found.
   * @param iName the internal name to get the class from.
   * @return the class if one is found.
   */
  public Class<?> getForIName(String iName) {
    if (iname2role.containsKey(iName)) {
      return iname2role.get(iName);
    }

    return iname2type.get(iName);
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
  public Class<? extends Entity> getTypeForXName(String xName) {
    return xname2type.get(xName);
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

  @SuppressWarnings("unchecked")
  public <T extends Role> Class<T> getBaseRole(Class<T> type) {
    if (type != null && type.getSuperclass() != Role.class) {
      return getBaseRole((Class<T>) type.getSuperclass());
    }
    return type;
  }

  /**
   * Convenience method that returns {@code getBaseClass} or {@code getBaseRole} or null depending on the parameter. 
   * @param type the type to get the base from. 
   * @return
   */
  public Class<?> getBase(Class<?> type) {
    if (isEntity(type)) {
      return getBaseClass(toEntity(type));
    } else if (isRole(type)) {
      return getBaseRole(toRole(type));
    }

    return null;
  }

  public Set<Class<? extends Entity>> getSubClasses(Class<? extends Entity> baseClass) {
    return subClassMap.get(baseClass);
  }

  /**
   * Gets a sub class of the primitive that correspondents with the {@code variation}.
   * @param typeForVariation should be a class of the model package.
   * @param variation should be a sub-package of the model package. 
   * @return the class if one is found, if not it returns null.
   */
  public Class<? extends Variable> getVariationClass(Class<? extends Variable> typeForVariation, String variation) {
    if (variation == null) {
      return null;
    }

    if (variationMap.containsKey(variation)) {
      for (Class<? extends Variable> variable : variationMap.get(variation)) {
        if (typeForVariation.isAssignableFrom(variable)) {
          return variable;
        }
      }
    }

    return null;
  }

  /**
   * Determines the variation of a class. This is based on the package the class is placed in.
   * @param type the type the variation should be determined of.
   * @return the variation. This will be null for each primitive (i.e. Person) and supporting classes (like DomainEntity).
   */
  public String getClassVariation(Class<? extends Variable> type) {
    String packageName = type.getPackage().getName();
    if (packageName.endsWith(".model")) {
      return null;
    }

    return packageName.substring(packageName.lastIndexOf('.') + 1);
  }

  public boolean isRole(String typeName) {
    return this.iname2role.containsKey(typeName);
  }

  // --- static utilities ----------------------------------------------

  public static boolean isEntity(Class<?> cls) {
    return cls == null ? false : Entity.class.isAssignableFrom(cls);
  }

  public static boolean isSystemEntity(Class<?> cls) {
    return cls == null ? false : SystemEntity.class.isAssignableFrom(cls);
  }

  public static boolean isDomainEntity(Class<?> cls) {
    return cls == null ? false : DomainEntity.class.isAssignableFrom(cls);
  }

  public static boolean isVariable(Class<?> cls) {
    return cls == null ? false : Variable.class.isAssignableFrom(cls);
  }

  public static boolean isRole(Class<?> cls) {
    return cls == null ? false : Role.class.isAssignableFrom(cls);
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

  @SuppressWarnings("unchecked")
  public static <T extends Role> Class<T> toRole(Class<?> type) {
    if (isRole(type)) {
      return (Class<T>) type;
    }
    throw new ClassCastException(type.getName() + " is not a domain entity");
  }

  // -------------------------------------------------------------------

  private static boolean superclass(Class<?> cls, Class<?> target, int level) {
    while (cls != null && level > 0) {
      cls = cls.getSuperclass();
      level--;
      if (cls == target) {
        return true;
      }
    }
    return false;
  }

  /**
   * Timbuctoo only accepts primitive system entities,
   * immediate subclasses of {@code SystemEntity}.
   */
  public static boolean isValidSystemEntity(Class<?> cls) {
    return superclass(cls, SystemEntity.class, 1);
  }

  /**
   * Timbuctoo only accepts primitive domain entities,
   * immediate subclasses of {@code DomainEntity},
   * and immediate subclasses of primitive domain entities.
   */
  public static boolean isValidDomainEntity(Class<?> cls) {
    return superclass(cls, DomainEntity.class, 2);
  }

}
