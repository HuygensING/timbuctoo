package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.RelationTypes.INVERSE_NAME_PROPERTY;
import static nl.knaw.huygens.timbuctoo.storage.RelationTypes.REGULAR_NAME_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.junit.Before;
import org.junit.Test;

public class RelationTypesTest {
  private static final String ID = "id";
  private static final String NAME = "name";
  private RelationTypes instance;
  private Storage storageMock;

  @Before
  public void setUp() {
    storageMock = mock(Storage.class);
    instance = new RelationTypes(storageMock);
  }

  @Test
  public void testGetByIdWhenExceptionOccurs() throws Exception {
    when(storageMock.getItem(RelationType.class, ID)).thenThrow(new StorageException());

    assertNull(instance.getById(ID));
    verify(storageMock).getItem(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsUnknown() throws Exception {
    when(storageMock.getItem(RelationType.class, ID)).thenReturn(null);

    assertNull(instance.getById(ID));
    verify(storageMock).getItem(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.getItem(RelationType.class, ID)).thenReturn(type);

    assertEquals(type, instance.getById(ID));
    verify(storageMock).getItem(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.getItem(RelationType.class, ID)).thenReturn(type);

    assertEquals(type, instance.getById(ID));
    assertEquals(type, instance.getById(ID));
    verify(storageMock, times(1)).getItem(RelationType.class, ID);
  }

  @Test
  public void testGetByNameWhenExceptionOccurs() throws Exception {
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenThrow(new StorageException());

    assertNull(instance.getByName(NAME));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsUnknown() throws Exception {
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(null);
    when(storageMock.findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME)).thenReturn(null);

    assertNull(instance.getByName(NAME));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
    verify(storageMock).findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemWhenInverseNameIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(null);
    when(storageMock.findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
    verify(storageMock).findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME));
    assertEquals(type, instance.getByName(NAME));

    verify(storageMock, times(1)).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }
}
