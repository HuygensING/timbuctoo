package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.util.ClassComparator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

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
 *
 * <p>The use of classes as type tokens is connected to type erasure
 * of Java generics. In addition to type tokens we use two string
 * representations of entity types:<ul>
 * <li>internal names, which are used, for instance, for indicating
 * entity types in JSON (so as to hide implementation details),
 * for Solr core names, and for Mongo collection names.</li>
 * <li>external names, which are used in the REST API for indicating
 * entity collections.</li>
 * </ul>
 * The conversion is implemented in class {@code TypeNames}.
 */
public class TypeRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(TypeRegistry.class);

  /** The unique instance of this class. */
  private static TypeRegistry instance;

  public static synchronized TypeRegistry getInstance() {
    if (instance == null) {
      instance = new TypeRegistry();
    }
    return instance;
  }

  // ---------------------------------------------------------------------------

  private final Set<Class<? extends SystemEntity>> systemEntities = Sets.newTreeSet(new ClassComparator());
  private final Set<Class<? extends DomainEntity>> domainEntities = Sets.newTreeSet(new ClassComparator());

  private final Map<String, Class<? extends SystemEntity>> iname2SystemType = Maps.newHashMap();
  private final Map<String, Class<? extends DomainEntity>> iname2DomainType = Maps.newHashMap();

  private final Map<String, Class<? extends Entity>> xname2type = Maps.newHashMap();

  private final Map<String, String> iname2xname = Maps.newHashMap();

  private final Map<Class<? extends Role>, String> role2iname = Maps.newHashMap();
  private final Map<String, Class<? extends Role>> iname2role = Maps.newHashMap();

  private final Map<Class<? extends Entity>, Set<Class<? extends Role>>> allowedRoles = Maps.newHashMap();

  private TypeRegistry() {}

  public void init(String packageNames) {
    checkArgument(packageNames != null, "'packageNames' must not be null");

    clear();
    ClassPath classPath = getClassPath();
    for (String packageName : StringUtils.split(packageNames)) {
      registerPackage(classPath, packageName.replaceFirst("^timbuctoo", "nl.knaw.huygens.timbuctoo"));
    }
  }

  /**
   * Clears all entries.
   */
  private void clear() {
    iname2SystemType.clear();
    iname2DomainType.clear();
    xname2type.clear();
    iname2xname.clear();
    role2iname.clear();
    iname2role.clear();
    allowedRoles.clear();
  }

  private ClassPath getClassPath() {
    try {
      return ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void registerPackage(ClassPath classPath, String packageName) {
    Set<Class<? extends Role>> roles = Sets.newHashSet();
    for (ClassInfo info : getClassInfoSet(classPath, packageName)) {
      Class<?> type = info.load();
      if (isEntity(type) && !shouldNotRegister(type)) {
        if (BusinessRules.isValidSystemEntity(type)) {
          registerSystemEntity(toSystemEntity(type));
        } else if (BusinessRules.isValidDomainEntity(type)) {
          Class<? extends DomainEntity> entityType = toDomainEntity(type);
          registerDomainEntity(entityType);
          if (!Relation.class.isAssignableFrom(entityType)) {
            allowedRoles.put(entityType, roles);
          }
        } else {
          LOG.error("Not a valid entity: '{}'", type.getName());
          throw new IllegalStateException("Invalid entity");
        }
        LOG.debug("Registered entity {}", type.getName());
      } else if (isRole(type) && !shouldNotRegister(type)) {
        if (BusinessRules.isValidRole(type)) {
          Class<? extends Role> roleType = toRole(type);
          registerRole(roleType);
          roles.add(roleType);
        } else {
          LOG.error("Not a valid role: '{}'", type.getName());
          throw new IllegalStateException("Invalid role");
        }
      }
    }
  }

  private Set<ClassInfo> getClassInfoSet(ClassPath classPath, String packageName) {
    if (packageName.endsWith(".*")) {
      return classPath.getTopLevelClassesRecursive(StringUtils.chomp(packageName, ".*"));
    } else {
      return classPath.getTopLevelClasses(packageName);
    }
  }

  private boolean shouldNotRegister(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers());
  }

  // ---------------------------------------------------------------------------

  private <T extends SystemEntity> void registerSystemEntity(Class<T> type) {
    systemEntities.add(type);

    String iname = TypeNames.getInternalName(type);
    if (iname2SystemType.containsKey(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    iname2SystemType.put(iname, type);
  }

  private <T extends DomainEntity> void registerDomainEntity(Class<T> type) {
    domainEntities.add(type);

    String iname = TypeNames.getInternalName(type);
    if (iname2DomainType.containsKey(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    iname2DomainType.put(iname, type);

    String xname = TypeNames.getExternalName(type);
    if (xname2type.containsKey(xname)) {
      throw new IllegalStateException("Duplicate internal type name " + xname);
    }
    xname2type.put(xname, type);

    iname2xname.put(iname, xname);
  }

  private <T extends Role> void registerRole(Class<T> role) {
    String iname = TypeNames.getInternalName(role);
    if (role2iname.containsValue(iname)) {
      throw new IllegalStateException("Duplicate internal type name " + iname);
    }
    role2iname.put(role, iname);
    iname2role.put(iname, role);
  }

  // --- public api ------------------------------------------------------------

  /**
   * Returns a list with all registered system entity types.
   */
  public List<Class<? extends SystemEntity>> getSystemEntityTypes() {
    return Lists.newArrayList(systemEntities);
  }

  /**
   * Returns a list with all registered domain entity types.
   */
  public List<Class<? extends DomainEntity>> getDomainEntityTypes() {
    return Lists.newArrayList(domainEntities);
  }

  /**
   * Returns a list with all registered primitive domain entity types.
   */
  public List<Class<? extends DomainEntity>> getPrimitiveDomainEntityTypes() {
    return Lists.newArrayList(Iterables.filter(domainEntities, new Predicate<Class<?>>() {
        @Override
        public boolean apply(Class<?> type) {
          return isPrimitiveDomainEntity(type);
        }
      }));
  }

  /**
   * Returns the system entity type token for the specified internal type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends SystemEntity> getSystemEntityType(String iname) {
    return iname2SystemType.get(iname);
  }

  /**
   * Returns the domain entity type token for the specified internal type name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends DomainEntity> getDomainEntityType(String iname) {
    return iname2DomainType.get(iname);
  }

  /**
   * Returns {@code true} if the specified internal type name corresponds
   * with a primitive domain entity type, {@code false} otherwise.
   */
  public boolean mapsToPrimitiveDomainEntity(String iname) {
    return isPrimitiveDomainEntity(iname2DomainType.get(iname));
  }

  /**
   * Returns the type token for the specified internal role name,
   * or {@code null} if there is no such token.
   */
  public Class<? extends Role> getRoleForIName(String iname) {
    return iname2role.get(iname);
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

  /**
   * Returns the types of the roles that may be assigned to an entity.
   */
  public Set<Class<? extends Role>> getAllowedRolesFor(Class<?> type) {
    Set<Class<? extends Role>> roles = allowedRoles.get(type);
    if (roles != null) {
      return roles;
    } else {
      return Collections.emptySet();
    }
  }

  // --- static utilities ------------------------------------------------------

  public static boolean isEntity(Class<?> cls) {
    return cls == null ? false : Entity.class.isAssignableFrom(cls);
  }

  public static boolean isSystemEntity(Class<?> cls) {
    return cls == null ? false : SystemEntity.class.isAssignableFrom(cls);
  }

  public static boolean isDomainEntity(Class<?> cls) {
    return cls == null ? false : DomainEntity.class.isAssignableFrom(cls);
  }

  public static boolean isPrimitiveDomainEntity(Class<?> cls) {
    return cls != null && cls.getSuperclass() == DomainEntity.class;
  }

  public static boolean isRole(Class<?> cls) {
    return cls == null ? false : Role.class.isAssignableFrom(cls);
  }

  /**
   * Forces the typecast of the specified class to a system entity type token.
   */
  @VisibleForTesting
  static <T extends SystemEntity> Class<T> toSystemEntity(Class<?> cls) throws ClassCastException {
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

  /**
   * Returns the primitive domain entity for the specified domain entity,
   * assuming that the two-level hierarchy is satisfied.
   */
  public static Class<? extends DomainEntity> toBaseDomainEntity(Class<? extends DomainEntity> type) throws ClassCastException {
    @SuppressWarnings("unchecked")
    Class<? extends DomainEntity> superType = (Class<? extends DomainEntity>) type.getSuperclass();
    return (superType == DomainEntity.class) ? type : superType;
  }

  @VisibleForTesting
  static <T extends Role> Class<T> toRole(Class<?> type) {
    if (isRole(type)) {
      @SuppressWarnings("unchecked")
      Class<T> result = (Class<T>) type;
      return result;
    }
    throw new ClassCastException(type.getName() + " is not a role");
  }

  public boolean isFromSameProject(Class<? extends DomainEntity> class1, Class<? extends DomainEntity> class2) {
    String package1 = class1.getPackage().getName();
    String package2 = class2.getPackage().getName();
    return Objects.equal(package1, package2);
  }

}
