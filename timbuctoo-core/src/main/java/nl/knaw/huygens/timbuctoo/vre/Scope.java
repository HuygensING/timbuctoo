package nl.knaw.huygens.timbuctoo.vre;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * Defines a subset of the data in the repository.
 */
public interface Scope {

  /**
   * Returns the unique id of this scope.
   */
  String getId();

  /**
   * Returns the name of this scope.
   */
  String getName();

  /**
   * Returns primitive domain entity types in this scope.
   */
  Set<Class<? extends DomainEntity>> getBaseEntityTypes();

  /**
   * Returns all domain entity types in this scope.
   */
  Set<Class<? extends DomainEntity>> getAllEntityTypes();

  /**
   * Returns {@code true} if the specified domain entity
   * is in scope, {@code false} otherwise.
   */
  <T extends DomainEntity> boolean inScope(Class<T> type, String id);

  /**
   * Returns {@code true} if the specified domain entity
   * is in scope, {@code false} otherwise.
   */
  <T extends DomainEntity> boolean inScope(T entity);

  /**
   * Checks if the {@code type} is in scope. A light method to check if it is needed to do a further check.
   * @param type the type to check if it is in scope.
   * @return {@code true} if the type is in scope {@code false} if not.
   */
  <T extends DomainEntity> boolean isTypeInScope(Class<T> type);

}
