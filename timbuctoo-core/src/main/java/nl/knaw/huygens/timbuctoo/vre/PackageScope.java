package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.ClassComparator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class PackageScope implements Scope {

  private Map<Class<? extends DomainEntity>, Class<? extends DomainEntity>> baseTypeScopeTypeMap;

  public PackageScope(String packageName) throws IOException {
    baseTypeScopeTypeMap = Maps.newHashMap();
    addPackage(packageName);
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

  @Override
  public Class<? extends DomainEntity> mapToScopeType(Class<? extends DomainEntity> baseType) throws NotInScopeException {
    if (!baseTypeScopeTypeMap.containsKey(baseType)) {
      throw NotInScopeException.noTypeMatchesBaseType(baseType);
    }

    return baseTypeScopeTypeMap.get(baseType);
  }

  @Override
  public final Set<Class<? extends DomainEntity>> getPrimitiveEntityTypes() {
    return Collections.unmodifiableSet(baseTypeScopeTypeMap.keySet());
  }

  @Override
  public Set<Class<? extends DomainEntity>> getEntityTypes() {
    return Collections.unmodifiableSet(Sets.newHashSet(baseTypeScopeTypeMap.values()));
  }

  /**
   * Default implementation: just test for entity type.
   */
  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return containsType(type);
  }

  /**
   * Default implementation: just test for entity type.
   */
  @Override
  public <T extends DomainEntity> boolean inScope(T entity) {
    return containsType(entity.getClass());
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type) {
    return containsType(type);
  }

  private <T extends DomainEntity> boolean containsType(Class<T> type) {
    return baseTypeScopeTypeMap.containsValue(type);
  }

  private final void addPackage(String name) throws IOException {
    ClassPath classPath = ClassPath.from(PackageScope.class.getClassLoader());
    String packageName = name.replaceFirst("^timbuctoo", "nl.knaw.huygens.timbuctoo");
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      addClass(info.load());
    }
  }

  private final void addClass(Class<?> cls) {
    if (TypeRegistry.isDomainEntity(cls) && cls != DomainEntity.class) {
      Class<DomainEntity> type = TypeRegistry.toDomainEntity(cls);
      Class<? extends DomainEntity> baseType = TypeRegistry.toBaseDomainEntity(type);
      baseTypeScopeTypeMap.put(baseType, type);
    }
  }
  private Builder<Class<? extends DomainEntity>> newBuilder() {
    return new Builder<>(new ClassComparator());
  }

}
