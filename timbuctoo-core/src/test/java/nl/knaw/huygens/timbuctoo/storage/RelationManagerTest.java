package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.junit.Test;

public class RelationManagerTest {

  @Test(expected = IllegalArgumentException.class)
  public void testGetRelationTypeWithWrongReference() {
    DocTypeRegistry registry = mock(DocTypeRegistry.class);
    StorageManager storageMgr = mock(StorageManager.class);
    RelationManager relationMgr = new RelationManager(registry, storageMgr);
    relationMgr.getRelationType(new Reference(DomainEntity.class, "id"));
  }

  @Test
  public void testGetRelationTypeWithCorrectReference() {
    DocTypeRegistry registry = mock(DocTypeRegistry.class);
    StorageManager storageMgr = mock(StorageManager.class);
    when(storageMgr.getEntity(RelationType.class, "id")).thenReturn(new RelationType());
    RelationManager relationMgr = new RelationManager(registry, storageMgr);
    assertNotNull(relationMgr.getRelationType(new Reference(RelationType.class, "id")));
  }

}
