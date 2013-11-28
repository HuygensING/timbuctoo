package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelationManagerTest {
  private StorageManager storageManager;
  private TypeRegistry typeRegistry;

  private RelationManager relationManager;

  @Before
  public void setUp() {
    typeRegistry = mock(TypeRegistry.class);
    storageManager = mock(StorageManager.class);
    relationManager = new RelationManager(typeRegistry, storageManager);
  }

  @After
  public void tearDown() {
    typeRegistry = null;
    storageManager = null;
    relationManager = null;
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

}
