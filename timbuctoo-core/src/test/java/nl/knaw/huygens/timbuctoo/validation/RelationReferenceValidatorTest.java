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

import org.junit.Test;

public class RelationReferenceValidatorTest {

  private RelationReferenceValidator validator;
  private Relation relation;

  private void setup(boolean sourceExists, boolean targetExists) {
    TypeRegistry registry = mock(TypeRegistry.class);
    doReturn(SourceType.class).when(registry).getDomainEntityType("sourceType");
    doReturn(TargetType.class).when(registry).getDomainEntityType("targetType");

    StorageManager storage = mock(StorageManager.class);
    when(storage.getTypeRegistry()).thenReturn(registry);
    when(storage.entityExists(SourceType.class, "sourceId")).thenReturn(sourceExists);
    when(storage.entityExists(TargetType.class, "targetId")).thenReturn(targetExists);

    validator = new RelationReferenceValidator(storage);

    relation = RelationBuilder.newInstance(Relation.class) //
        .withSourceId("sourceId") //
        .withSourceType("sourceType") //
        .withTargetId("targetId") //
        .withTargetType("targetType") //
        .build();
  }

  @Test
  public void testValidateIsValid() throws Exception {
    setup(true, true);
    validator.validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateSourceDoesNotExist() throws Exception {
    setup(false, true);
    validator.validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateTargetDoesNotExist() throws Exception {
    setup(true, false);
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
