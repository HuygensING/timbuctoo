package nl.knaw.huygens.timbuctoo.vre;

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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.ClassComparator;

import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class PackageScope implements Scope {

  private ClassPath classPath;
  private Builder<Class<? extends DomainEntity>> builder;

  private Set<Class<? extends DomainEntity>> allTypes;
  private Set<Class<? extends DomainEntity>> baseTypes;

  public PackageScope() throws IOException {
    classPath = ClassPath.from(PackageScope.class.getClassLoader());
    builder = newBuilder();
  }

  @Override
  public <T extends DomainEntity> List<T> filter(final List<T> entities) {
    List<T> filteredList = Lists.newArrayList();

    for (T entity : entities) {
      if (inScope(entity)) {
        filteredList.add(entity);
      }
    }

    return filteredList;
  }

  /**
   * Convenience constructor that creates a scope for a single package.
   */
  public PackageScope(String packageName) throws IOException {
    this();
    addPackage(packageName);
    buildTypes();
  }

  @Override
  public final Set<Class<? extends DomainEntity>> getBaseEntityTypes() {
    checkState(builder == null);
    return baseTypes;
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
  public <T extends DomainEntity> boolean inScope(Class<T> type) {
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
    return new Builder<Class<? extends DomainEntity>>(new ClassComparator());
  }

  private Set<Class<? extends DomainEntity>> buildBaseTypes() {
    Builder<Class<? extends DomainEntity>> builder = newBuilder();
    for (Class<? extends DomainEntity> type : allTypes) {
      builder.add(TypeRegistry.toBaseDomainEntity(type));
    }
    return builder.build();
  }

}
