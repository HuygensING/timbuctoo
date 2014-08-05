package nl.knaw.huygens.timbuctoo.model;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.util.RelationRefCreator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DomainEntityTest {

  private static final String ENTITY_ID = "id";
  private static final Class<Relation> RELATION_TYPE = Relation.class;
  private static final int RELATION_LIMIT = 5;
  private DomainEntity entity;
  private double relationNumber = 0;
  private EntityMapper entityMapperMock;
  private RelationRefCreator relationRefCreatorMock;
  private EntityMappers entityMappersMock;

  @Before
  public void setUp() {
    entity = new DomainEntity() {
      @Override
      public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    entity.setId(ENTITY_ID);
    setupEntityMappers();
    relationRefCreatorMock = mock(RelationRefCreator.class);
  }

  @Test
  public void getVariationsCannotBeNull() {
    assertNotNull(entity.getVariations());

    entity.setVariations(null);
    assertNotNull(entity.getVariations());
  }

  @Test
  public void setVariationsRemovesExistingItems() {
    assertEquals(0, entity.getVariations().size());

    entity.addVariation("a");
    assertEquals(1, entity.getVariations().size());

    entity.setVariations(null);
    assertEquals(0, entity.getVariations().size());
  }

  @Test
  public void setVariationsEliminatesDuplicates() {
    entity.setVariations(Lists.newArrayList("a", "b", "a", "b", "b"));
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());
  }

  @Test
  public void addVariationEliminatesDuplicates() {
    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a"), entity.getVariations());

    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a"), entity.getVariations());

    entity.addVariation("b");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());

    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());

    entity.addVariation("b");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());
  }

  @Test
  public void addRelationsGetsTheRelationsFromTheDatabaseAndAddsAReferenceToThisDomainEntity() throws StorageException {
    // setup
    boolean isRegularRelation = true;
    boolean isInverseRegularRelation = false;

    Relation relation1 = createRelationWhereEntityIsSource(ENTITY_ID);
    Relation relation2 = createRelationWhereEntityIsTarget(ENTITY_ID);
    Repository repositoryMock = setupRepository(relation1, relation2);

    // action
    entity.addRelations(repositoryMock, RELATION_LIMIT, entityMappersMock, relationRefCreatorMock);

    // verify
    verify(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);
    verifyRelationRefIsCreatedForRelation(relation1, isRegularRelation);
    verifyRelationRefIsCreatedForRelation(relation2, isInverseRegularRelation);
    assertThat(entity.getRelationCount(), equalTo(2));
  }

  private Repository setupRepository(Relation... relations) throws StorageException {
    Repository repositoryMock = mock(Repository.class);
    doReturn(Lists.newArrayList(relations)).when(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);
    when(repositoryMock.getRelationTypeById(anyString())).thenReturn(mock(RelationType.class));
    when(repositoryMock.getRelationTypeById(anyString())).thenReturn(mock(RelationType.class));

    return repositoryMock;
  }

  @Test(expected = StorageException.class)
  public void addRelationsThrowsAnExceptionIfRepositoryThrowsAnExceptionWhileRetrievingTheRelations() throws StorageException {
    // setup
    Relation relation1 = createRelationWhereEntityIsSource(ENTITY_ID);
    Relation relation2 = createRelationWhereEntityIsTarget(ENTITY_ID);
    Repository repositoryMock = mock(Repository.class);
    doReturn(Lists.newArrayList(relation1, relation2)).when(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);

    doThrow(StorageException.class).when(repositoryMock).getRelationTypeById(anyString());

    try {
      entity.addRelations(repositoryMock, RELATION_LIMIT, entityMappersMock, relationRefCreatorMock);
    } finally {
      verify(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);
      verifyZeroInteractions(relationRefCreatorMock);
    }

  }

  @Test(expected = StorageException.class)
  public void addRelationsThrowsAnExceptionIfRepositoryThrowsAnExceptionWhileRetrievingRelationTypes() throws StorageException {
    // setup
    Repository repositoryMock = mock(Repository.class);
    doThrow(StorageException.class).when(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);

    try {
      entity.addRelations(repositoryMock, RELATION_LIMIT, entityMappersMock, relationRefCreatorMock);
    } finally {
      verify(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);
      verifyZeroInteractions(relationRefCreatorMock);
    }

  }

  @Test(expected = StorageException.class)
  public void addRelationsThrowsAnExceptionIfRelationRefCreatorThrowsAnException() throws StorageException {
    // setup
    Relation relation1 = createRelationWhereEntityIsSource(ENTITY_ID);
    Relation relation2 = createRelationWhereEntityIsTarget(ENTITY_ID);

    Repository repositoryMock = setupRepository(relation1, relation2);

    doThrow(StorageException.class).when(relationRefCreatorMock).newRelationRef(any(EntityMapper.class), any(Reference.class), anyString(), anyBoolean(), anyInt());
    try {
      // action
      entity.addRelations(repositoryMock, RELATION_LIMIT, entityMappersMock, relationRefCreatorMock);
    } finally {
      verify(repositoryMock).getRelationsByEntityId(ENTITY_ID, RELATION_LIMIT, RELATION_TYPE);
      verify(relationRefCreatorMock).newRelationRef(any(EntityMapper.class), any(Reference.class), anyString(), anyBoolean(), anyInt());
    }
  }

  private EntityMappers setupEntityMappers() {
    entityMappersMock = mock(EntityMappers.class);
    entityMapperMock = mock(EntityMapper.class);
    when(entityMappersMock.getEntityMapper(entity.getClass())).thenReturn(entityMapperMock);
    doReturn(RELATION_TYPE).when(entityMapperMock).map(RELATION_TYPE);

    return entityMappersMock;
  }

  private void verifyRelationRefIsCreatedForRelation(Relation relation, boolean isRegularRelation) throws StorageException {
    Reference ref = null;
    if (isRegularRelation) {
      ref = relation.getTargetRef();
    } else {
      ref = relation.getSourceRef();
    }
    verify(relationRefCreatorMock).newRelationRef(entityMapperMock, ref, relation.getId(), relation.isAccepted(), relation.getRev());
  }

  protected Relation createRelationWhereEntityIsSource(String sourceId) {
    return createRelation(sourceId, "targetId" + relationNumber, ENTITY_ID + relationNumber);
  }

  protected Relation createRelationWhereEntityIsTarget(String targetId) {
    return createRelation("sourceId" + relationNumber, targetId, ENTITY_ID + relationNumber);
  }

  protected Relation createRelation(String sourceId, String targetId, String id) {
    relationNumber++;

    Relation relation = new Relation();
    relation.setSourceId(sourceId);
    relation.setSourceType("sourceType");
    relation.setTargetId(targetId);
    relation.setTargetType("targetType");
    relation.setTypeId("typeId" + relationNumber);
    relation.setId(id);
    return relation;
  }
}
