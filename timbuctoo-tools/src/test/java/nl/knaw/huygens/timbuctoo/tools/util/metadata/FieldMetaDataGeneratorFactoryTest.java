package nl.knaw.huygens.timbuctoo.tools.util.metadata;

/*
 * #%L
 * Timbuctoo tools
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.tools.util.metadata.TypeFacade.FieldType;

import org.junit.Before;
import org.junit.Test;

public class FieldMetaDataGeneratorFactoryTest {
  private TypeFacade containingTypeMock;
  private FieldMetaDataGeneratorFactory instance;

  @Before
  public void setUp() {
    TypeNameGenerator typeNameGeneratorMock = mock(TypeNameGenerator.class);
    containingTypeMock = mock(TypeFacade.class);
    instance = new FieldMetaDataGeneratorFactory(typeNameGeneratorMock);
  }

  private void testCreateFieldTypeResultsInGenerator(FieldType fieldType, Class<? extends FieldMetaDataGenerator> expectedGeneratorType) {
    Field field = null;

    // when 
    when(containingTypeMock.getFieldType(any(Field.class))).thenReturn(fieldType);

    // action
    FieldMetaDataGenerator metaDataGenerator = instance.create(field, containingTypeMock);

    // verify
    verify(containingTypeMock).getFieldType(field);
    assertThat(metaDataGenerator, is(instanceOf(expectedGeneratorType)));
  }

  @Test
  public void testCreateForEnumField() {
    testCreateFieldTypeResultsInGenerator(FieldType.ENUM, EnumValueFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForConstantField() {
    testCreateFieldTypeResultsInGenerator(FieldType.CONSTANT, ConstantFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForPoorMansEnumField() {
    testCreateFieldTypeResultsInGenerator(FieldType.POOR_MANS_ENUM, PoorMansEnumFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForDefaultField() {
    testCreateFieldTypeResultsInGenerator(FieldType.DEFAULT, DefaultFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForUnknownTypeField() {
    testCreateFieldTypeResultsInGenerator(FieldType.UNKNOWN, NoOpFieldMetaDataGenerator.class);
  }
}
