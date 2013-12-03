package nl.knaw.huygens.timbuctoo.config;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

/**
 * Encapsulates business rules for handling of entities.
 */
public class BusinessRules {

  // --- API -----------------------------------------------------------

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

  /**
   * Timbuctoo only accepts primitive roles,
   * immediate subclasses of {@code Role},
   * and immediate subclasses of primitive roles.
   */
  public static boolean isValidRole(Class<?> cls) {
    return superclass(cls, Role.class, 2);
  }

  /**
   * Can a system entity with the specfied type be added to the data store.
   */
  public static boolean allowSystemEntityAdd(Class<?> type) {
    return type != null && type.getSuperclass() == SystemEntity.class;
  }

  /**
   * Can a domain entity with the specfied type be added to the data store.
   */
  public static boolean allowDomainEntityAdd(Class<?> type) {
    return type != null && type.getSuperclass() != null && type.getSuperclass().getSuperclass() == DomainEntity.class;
  }

  /**
   * Can a role with the specfied type be added, as part of a domain entity, to the data store.
   */
  public static boolean allowRoleAdd(Class<?> type) {
    return type != null && type.getSuperclass() != null && type.getSuperclass().getSuperclass() == Role.class;
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

}
