package nl.knaw.huygens.timbuctoo.util;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

import test.variation.model.projecta.ProjectADomainEntity;
import test.variation.model.projecta.ProjectARelation;

public class RelationRefAdderTest {
  private static final RelationType RELATION_TYPE = new RelationType();
  private static final String TARGET_ID = "targetId";
  private static final String SOURCE_ID = "sourceId";
  private static final String RELATION_NAME = "relName";
  private EntityMapper mapper;
  private RelationRefAdder instance;
  private RelationRefCreator relationRefCreator;
  private ProjectARelation relation;

  @Before
  public void setUp() {
    relation = new ProjectARelation();
    relation.setSourceId(SOURCE_ID);
    relation.setTargetId(TARGET_ID);

    relationRefCreator = mock(RelationRefCreator.class);
    instance = new RelationRefAdder(relationRefCreator);
  }

  @Test
  public void addRelationCallsTheRelationRefCreatorsCreateRegularIfTheEntityIsTheSource() throws Exception {
    // setup
    ProjectADomainEntity entityToAddTo = new ProjectADomainEntity();
    entityToAddTo.setId(SOURCE_ID);

    RelationRef createdRef = regularRelationWithNameCreated(RELATION_NAME);

    // action
    instance.addRelation(entityToAddTo, mapper, relation, RELATION_TYPE);

    // verify
    verify(relationRefCreator).createRegular(mapper, relation, RELATION_TYPE);

    assertThat(entityToAddTo.getRelations(RELATION_NAME), contains(createdRef));
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheRelationRefCreatorCreateRegularDoes() throws Exception {
    // setup
    ProjectADomainEntity entityToAddTo = new ProjectADomainEntity();
    entityToAddTo.setId(SOURCE_ID);
    when(relationRefCreator.createRegular(mapper, relation, RELATION_TYPE)).thenThrow(new StorageException());

    // action
    instance.addRelation(entityToAddTo, mapper, relation, RELATION_TYPE);
  }

  private RelationRef regularRelationWithNameCreated(String name) throws StorageException {
    RelationRef relationRef = relationRefWithName(name);
    when(relationRefCreator.createRegular(mapper, relation, RELATION_TYPE)).thenReturn(relationRef);
    return relationRef;
  }

  private RelationRef relationRefWithName(String name) {
    RelationRef relationRef = new RelationRef();
    relationRef.setRelationName(name);
    relationRef.setDisplayName("displayName");
    return relationRef;
  }

  @Test
  public void addRelationCallsTheRelationRefCreatorsCreateInverseIfTheEntityIsTheTarget() throws Exception {
    // setup
    ProjectADomainEntity entityToAddTo = new ProjectADomainEntity();
    entityToAddTo.setId(TARGET_ID);

    RelationRef createdRef = inverseRelationWithNameCreated(RELATION_NAME);

    // action
    instance.addRelation(entityToAddTo, mapper, relation, RELATION_TYPE);

    // verify
    verify(relationRefCreator).createInverse(mapper, relation, RELATION_TYPE);

    assertThat(entityToAddTo.getRelations(RELATION_NAME), contains(createdRef));
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheRelationRefCreatorCreateInverseDoes() throws Exception {
    // setup
    ProjectADomainEntity entityToAddTo = new ProjectADomainEntity();
    entityToAddTo.setId(TARGET_ID);
    when(relationRefCreator.createInverse(mapper, relation, RELATION_TYPE)).thenThrow(new StorageException());

    // action
    instance.addRelation(entityToAddTo, mapper, relation, RELATION_TYPE);
  }

  private RelationRef inverseRelationWithNameCreated(String name) throws StorageException {
    RelationRef relationRef = relationRefWithName(name);
    when(relationRefCreator.createInverse(mapper, relation, RELATION_TYPE)).thenReturn(relationRef);
    return relationRef;
  }

}
