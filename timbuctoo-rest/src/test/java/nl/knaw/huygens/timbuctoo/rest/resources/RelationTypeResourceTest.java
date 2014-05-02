package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.Before;
import org.junit.Test;








import test.model.BaseDomainEntity;
import test.model.DomainEntityWithMiscTypes;
import test.model.PrimitiveDomainEntity;
import test.model.projecta.OtherADomainEntity;
import test.model.projecta.SubADomainEntity;
import test.model.projectb.SubBDomainEntity;

import com.google.common.collect.Lists;

public class RelationTypeResourceTest {

  private TypeRegistry registry;
  private StorageManager storageManager;
  private RelationTypeResource resource;

  @Before
  public void setup() {
    registry = mock(TypeRegistry.class);
    storageManager = mock(StorageManager.class);
    resource = new RelationTypeResource(registry, storageManager);
  }

  @Test
  public void testGetRelationTypeForEntityNoName() {
    // mock
    List<RelationType> types = Lists.newArrayList(mock(RelationType.class), mock(RelationType.class));
    List<Boolean> hasNext = Lists.newArrayList(true, true, false);

    // when
    StorageIterator<RelationType> iterator = createStorageIterator(types, hasNext);
    when(storageManager.getAll(RelationType.class)).thenReturn(iterator);

    // action
    List<RelationType> returnedRelationTypes = resource.getRelationTypesForEntity(null);

    // verify
    verify(iterator).close();
    assertThat(returnedRelationTypes, contains(types.toArray(new RelationType[0])));
  }

  @Test
  public void testGetRelationTypeForEntityPrimitive() {
    // when
    RelationType type1 = createRelationType(BaseDomainEntity.class, SubADomainEntity.class, false);
    RelationType type2 = createRelationType(DomainEntityWithMiscTypes.class, DomainEntity.class, false);
    RelationType type3 = createRelationType(DomainEntityWithMiscTypes.class, SubADomainEntity.class, false);

    List<RelationType> types = Lists.newArrayList(type1, type2, type3);
    List<Boolean> hasNext = Lists.newArrayList(true, true, true, false);

    StorageIterator<RelationType> iterator = createStorageIterator(types, hasNext);
    when(storageManager.getAll(RelationType.class)).thenReturn(iterator);

    setupGetTypeForInName(BaseDomainEntity.class);
    setupGetTypeForInName(SubADomainEntity.class);
    setupGetTypeForInName(DomainEntityWithMiscTypes.class);
    setupGetTypeForInName(DomainEntity.class);

    // action
    List<RelationType> actualRelationTypes = resource.getRelationTypesForEntity(TypeNames.getInternalName(BaseDomainEntity.class));

    // verify
    verify(iterator).close();
    assertThat(actualRelationTypes, contains(type1, type2));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetRelationTypeForEntityProjectSpecific() {
    // return all relations for the type and it's super classes except the ones that refer to a different project.
    // when
    RelationType type1 = createRelationType(BaseDomainEntity.class, SubADomainEntity.class, false);
    RelationType type2 = createRelationType(SubBDomainEntity.class, DomainEntity.class, false);
    RelationType type3 = createRelationType(PrimitiveDomainEntity.class, DomainEntity.class, false);
    RelationType type4 = createRelationType(OtherADomainEntity.class, DomainEntity.class, false);

    List<RelationType> types = Lists.newArrayList(type1, type2, type3, type4);
    List<Boolean> hasNext = Lists.newArrayList(true, true, true, true, false);

    StorageIterator<RelationType> iterator = createStorageIterator(types, hasNext);
    when(storageManager.getAll(RelationType.class)).thenReturn(iterator);

    setupGetTypeForInName(BaseDomainEntity.class);
    setupGetTypeForInName(SubADomainEntity.class);
    setupGetTypeForInName(PrimitiveDomainEntity.class);
    setupGetTypeForInName(DomainEntity.class);
    setupGetTypeForInName(SubBDomainEntity.class);
    setupGetTypeForInName(OtherADomainEntity.class);

    when(registry.isFromSameProject(any(Class.class), any(Class.class))).thenReturn(false);
    when(registry.isFromSameProject(SubADomainEntity.class, OtherADomainEntity.class)).thenReturn(true);
    when(registry.isFromSameProject(SubADomainEntity.class, SubADomainEntity.class)).thenReturn(true);

    // action
    List<RelationType> actualRelationTypes = resource.getRelationTypesForEntity(TypeNames.getInternalName(SubADomainEntity.class));

    // verify
    verify(iterator).close();
    verify(registry).isFromSameProject(SubADomainEntity.class, SubBDomainEntity.class);
    verify(registry).isFromSameProject(SubADomainEntity.class, OtherADomainEntity.class);
    assertThat(actualRelationTypes, contains(type1, type3, type4));
  }

  private RelationType createRelationType(Class<? extends DomainEntity> sourceClass, Class<? extends DomainEntity> targetClass, boolean symmetric) {
    RelationType relationType = new RelationType();
    relationType.setSourceTypeName(TypeNames.getInternalName(sourceClass));
    relationType.setTargetTypeName(TypeNames.getInternalName(targetClass));
    relationType.setSymmetric(symmetric);
    return relationType;
  }

  private void setupGetTypeForInName(Class<? extends DomainEntity> type) {
    doReturn(type).when(registry).getTypeForIName(TypeNames.getInternalName(type));
  }

  private StorageIterator<RelationType> createStorageIterator(List<RelationType> relationTypes, List<Boolean> hasNext) {
    @SuppressWarnings("unchecked")
    StorageIterator<RelationType> iterator = mock(StorageIterator.class);
    when(iterator.next()).thenReturn(relationTypes.get(0), relationTypes.subList(1, relationTypes.size()).toArray(new RelationType[0]));
    when(iterator.hasNext()).thenReturn(hasNext.get(0), hasNext.subList(1, hasNext.size()).toArray(new Boolean[0]));
    return iterator;
  }

}
