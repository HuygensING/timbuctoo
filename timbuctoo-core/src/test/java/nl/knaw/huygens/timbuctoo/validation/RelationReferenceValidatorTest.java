package nl.knaw.huygens.timbuctoo.validation;

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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class RelationReferenceValidatorTest {

  private String sourceId = "sourceId";
  private String sourceTypeString = "sourceType";
  private Class<SourceType> sourceType = SourceType.class;
  private String targetId = "targetId";
  private String targetTypeString = "targetType";
  private Class<TargetType> targetType = TargetType.class;
  private Relation relation;
  private TypeRegistry typeRegistryMock;
  private StorageManager storage;
  private RelationReferenceValidator validator;

  @Before
  public void setup() {
    typeRegistryMock = mock(TypeRegistry.class);
    storage = mock(StorageManager.class);
    validator = new RelationReferenceValidator(typeRegistryMock, storage);

    relation = RelationBuilder.newInstance(Relation.class) //
        .withSourceId(sourceId) //
        .withSourceType(sourceTypeString) //
        .withTargetId(targetId) //
        .withTargetType(targetTypeString) //
        .build();

    setupTypeRegistry();
  }

  private void setupTypeRegistry() {
    doReturn(sourceType).when(typeRegistryMock).getDomainEntityType(sourceTypeString);
    doReturn(targetType).when(typeRegistryMock).getDomainEntityType(targetTypeString);
  }

  @Test
  public void testValidateIsValid() throws Exception {
    // when
    when(storage.getEntity(sourceType, sourceId)).thenReturn(new SourceType());
    when(storage.getEntity(targetType, targetId)).thenReturn(new TargetType());

    // action
    validator.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storage);
    inOrder.verify(typeRegistryMock).getDomainEntityType(sourceTypeString);
    inOrder.verify(storage).getEntity(sourceType, sourceId);
    inOrder.verify(typeRegistryMock).getDomainEntityType(targetTypeString);
    inOrder.verify(storage).getEntity(targetType, targetId);
  }

  @Test(expected = ValidationException.class)
  public void testValidateSourceDoesNotExist() throws Exception {
    when(storage.getEntity(sourceType, sourceId)).thenReturn(null);
    when(storage.getEntity(targetType, targetId)).thenReturn(new TargetType());
    validator.validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateTargetDoesNotExist() throws Exception {
    when(storage.getEntity(sourceType, sourceId)).thenReturn(new SourceType());
    when(storage.getEntity(targetType, targetId)).thenReturn(null);
    validator.validate(relation);
  }

  private static class SourceType extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class TargetType extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

}
