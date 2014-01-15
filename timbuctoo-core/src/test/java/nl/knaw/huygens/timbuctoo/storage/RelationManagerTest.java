package nl.knaw.huygens.timbuctoo.storage;

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


import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectARelation;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestDocWithPersonName;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RelationManagerTest {

  private static TypeRegistry registry;

  private StorageManager storageManager;
  private RelationManager relationManager;
  private Change change;

  @BeforeClass
  public static void setupRegistry() {
    registry = TypeRegistry.getInstance();
    registry.init("timbuctoo.variation.model timbuctoo.model timbuctoo.variation.model.projecta");
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  @Before
  public void setUp() {
    storageManager = mock(StorageManager.class);
    relationManager = new RelationManager(registry, storageManager);
    change = new Change("test", "test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRelationTypeWithWrongReference() {
    relationManager.getRelationType(new Reference(DomainEntity.class, "id"));
  }

  @Test
  public void testGetRelationTypeWithCorrectReference() {
    when(storageManager.getEntity(RelationType.class, "id")).thenReturn(new RelationType());

    assertNotNull(relationManager.getRelationType(new Reference(RelationType.class, "id")));
  }

  @Test
  public void testStoreRelation() throws IOException {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "test");
    Reference targetRef = new Reference(ProjectATestDocWithPersonName.class, "test23");

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);

    verify(storageManager).addDomainEntity(ProjectARelation.class, expectedRelation, change);
  }

  @Test
  public void testStoreSymetricRelation() throws IOException {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectADomainEntity.class, relationTypeId, true);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "test");
    Reference targetRef = new Reference(ProjectADomainEntity.class, "test23");

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);

    verify(storageManager).addDomainEntity(ProjectARelation.class, expectedRelation, change);
  }

  @Test
  public void testStoreSymetricRelationSwitchIds() throws IOException {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectADomainEntity.class, relationTypeId, true);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "zztest23");
    Reference targetRef = new Reference(ProjectADomainEntity.class, "test");

    ProjectARelation expectedRelation = new ProjectARelation(targetRef, typeRef, sourceRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);

    verify(storageManager).addDomainEntity(ProjectARelation.class, expectedRelation, change);
  }

  @Test(expected = NullPointerException.class)
  public void testStoreRelationSourceRefNull() {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = null;
    Reference targetRef = new Reference(ProjectATestDocWithPersonName.class, "test23");

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);
  }

  @Test(expected = NullPointerException.class)
  public void testStoreRelationTypeRefNull() {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = null;
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "test");
    Reference targetRef = new Reference(ProjectATestDocWithPersonName.class, "test23");

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);
  }

  @Test(expected = NullPointerException.class)
  public void testStoreRelationTargetRefNull() {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "test");
    Reference targetRef = null;

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStoreRelationSourceRefWrongType() {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectATestDocWithPersonName.class, "test");
    Reference targetRef = new Reference(ProjectATestDocWithPersonName.class, "test23");

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStoreRelationTargetRefWrongType() {
    String relationTypeId = "relationTypeId";
    setUpGetRelationType(ProjectADomainEntity.class, ProjectATestDocWithPersonName.class, relationTypeId, false);

    Reference typeRef = new Reference(RelationType.class, relationTypeId);
    Reference sourceRef = new Reference(ProjectADomainEntity.class, "test");
    Reference targetRef = new Reference(ProjectADomainEntity.class, "test23");

    ProjectARelation expectedRelation = new ProjectARelation(sourceRef, typeRef, targetRef);
    expectedRelation.setAccepted(true);

    relationManager.storeRelation(ProjectARelation.class, sourceRef, typeRef, targetRef, change);
  }

  protected void setUpGetRelationType(Class<? extends DomainEntity> sourceClass, Class<? extends DomainEntity> targetClass, String relationTypeId, boolean symmetric) {
    RelationType relationType = new RelationType();
    relationType.setSourceTypeName(TypeNames.getInternalName(sourceClass));
    relationType.setTargetDocType(TypeNames.getInternalName(targetClass));
    relationType.setSymmetric(symmetric);

    when(storageManager.getEntity(RelationType.class, relationTypeId)).thenReturn(relationType);
  }

}

