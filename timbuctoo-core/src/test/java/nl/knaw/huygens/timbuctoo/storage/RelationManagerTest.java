package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectARelation;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestDocWithPersonName;

import org.junit.Before;
import org.junit.Test;

public class RelationManagerTest {

  private TypeRegistry typeRegistry;
  private StorageManager storageManager;
  private RelationManager relationManager;
  private Change change;

  @Before
  public void setUp() {
    typeRegistry = new TypeRegistry("timbuctoo.variation.model timbuctoo.model timbuctoo.variation.model.projecta");
    storageManager = mock(StorageManager.class);
    relationManager = new RelationManager(typeRegistry, storageManager);
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
    relationType.setSourceDocType(sourceClass);
    relationType.setTargetDocType(targetClass);
    relationType.setSymmetric(symmetric);

    when(storageManager.getEntity(RelationType.class, relationTypeId)).thenReturn(relationType);
  }

}
