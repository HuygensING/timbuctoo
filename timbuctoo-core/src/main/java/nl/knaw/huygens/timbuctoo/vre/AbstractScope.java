package nl.knaw.huygens.timbuctoo.vre;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public abstract class AbstractScope implements Scope {

  private ClassPath classPath;
  private Builder<Class<? extends DomainEntity>> builder;

  private Set<Class<? extends DomainEntity>> baseTypes;
  private Set<Class<? extends DomainEntity>> allTypes;

  public AbstractScope() throws IOException {
    classPath = ClassPath.from(AbstractScope.class.getClassLoader());
    builder = newBuilder();
  }

  /**
   * Convenience constructor that creates a scope for a single package.
   */
  public AbstractScope(String packageName) throws IOException {
    this();
    addPackage(packageName);
    buildTypes();
  }

  @Override
  public final Set<Class<? extends DomainEntity>> getBaseEntityTypes() {
    checkState(builder == null);
    return baseTypes;
  }

  @Override
  public final Set<Class<? extends DomainEntity>> getAllEntityTypes() {
    checkState(builder == null);
    return allTypes;
  }

  /**
   * Default implementation: just test for entity type.
   */
  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return allTypes.contains(type);
  }

  /**
   * Default implementation: just test for entity type.
   */
  @Override
  public <T extends DomainEntity> boolean inScope(T entity) {
    return allTypes.contains(entity.getClass());
  }

  @Override
  public <T extends DomainEntity> boolean isTypeInScope(Class<T> type) {
    return allTypes.contains(type);
  }

  protected final void addPackage(String name) throws IOException {
    checkState(builder != null);
    String packageName = name.replaceFirst("^timbuctoo", "nl.knaw.huygens.timbuctoo");
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      addClass(info.load());
    }
  }

  protected final void addClass(Class<?> cls) {
    checkState(builder != null);
    if (TypeRegistry.isDomainEntity(cls) && cls != DomainEntity.class) {
      builder.add(TypeRegistry.toDomainEntity(cls));
    }
  }

  protected final void buildTypes() {
    checkState(builder != null);
    allTypes = builder.build();
    baseTypes = buildBaseTypes();
    builder = null;
    classPath = null;
  }

  private Builder<Class<? extends DomainEntity>> newBuilder() {
    return new Builder<Class<? extends DomainEntity>>(new SimpleNameComparator());
  }

  private Set<Class<? extends DomainEntity>> buildBaseTypes() {
    Builder<Class<? extends DomainEntity>> builder = newBuilder();
    for (Class<? extends DomainEntity> type : allTypes) {
      builder.add(getBaseType(type));
    }
    return builder.build();
  }

  /**
   * Returns the primitive type for the specified domain entity type,
   * defined as the entity immediately below {@code DomainEntity} in
   * the class hierarchy.
   */
  private Class<? extends DomainEntity> getBaseType(Class<? extends DomainEntity> type) {
    checkArgument(type != null && type != DomainEntity.class);
    Class<? extends DomainEntity> superType = TypeRegistry.toDomainEntity(type.getSuperclass());
    return (superType == DomainEntity.class) ? type : getBaseType(superType);
  }

  // -------------------------------------------------------------------

  /**
   * Compares {@code DomainEntity} instances using their simple class name.
   */
  private static class SimpleNameComparator implements Comparator<Class<? extends DomainEntity>> {
    @Override
    public int compare(Class<? extends DomainEntity> o1, Class<? extends DomainEntity> o2) {
      return o1.getSimpleName().compareTo(o2.getSimpleName());
    }
  }

}
