package nl.knaw.huygens.timbuctoo.storage;

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
    when(storageMock.getEntity(RelationType.class, ID)).thenThrow(new StorageException());

    assertNull(instance.getById(ID, false));
    verify(storageMock).getEntity(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsUnknown() throws Exception {
    when(storageMock.getEntity(RelationType.class, ID)).thenReturn(null);

    assertNull(instance.getById(ID, false));
    verify(storageMock).getEntity(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.getEntity(RelationType.class, ID)).thenReturn(type);

    assertEquals(type, instance.getById(ID, false));
    verify(storageMock).getEntity(RelationType.class, ID);
  }

  @Test
  public void testGetByIdWhenItemIsInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.getEntity(RelationType.class, ID)).thenReturn(type);

    assertEquals(type, instance.getById(ID, false));
    assertEquals(type, instance.getById(ID, false));
    verify(storageMock, times(1)).getEntity(RelationType.class, ID);
  }

  @Test
  public void testGetByNameWhenExceptionOccurs() throws Exception {
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenThrow(new StorageException());

    assertNull(instance.getByName(NAME, false));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsUnknown() throws Exception {
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(null);
    when(storageMock.findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME)).thenReturn(null);

    assertNull(instance.getByName(NAME, false));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
    verify(storageMock).findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME, false));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemWhenInverseNameIsNotInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(null);
    when(storageMock.findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME, false));
    verify(storageMock).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
    verify(storageMock).findItemByProperty(RelationType.class, INVERSE_NAME_PROPERTY, NAME);
  }

  @Test
  public void testGetByNameWhenItemIsInCache() throws Exception {
    RelationType type = new RelationType();
    when(storageMock.findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME)).thenReturn(type);

    assertEquals(type, instance.getByName(NAME, false));
    assertEquals(type, instance.getByName(NAME, false));

    verify(storageMock, times(1)).findItemByProperty(RelationType.class, REGULAR_NAME_PROPERTY, NAME);
  }

}
