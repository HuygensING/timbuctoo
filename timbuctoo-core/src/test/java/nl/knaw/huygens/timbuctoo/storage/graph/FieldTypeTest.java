package nl.knaw.huygens.timbuctoo.storage.graph;

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

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.ADMINISTRATIVE;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class FieldTypeTest {

  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String INTERNAL_TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final String FIELD_NAME = "fieldName";

  @Test
  public void propertyNameOfADMINISTRATIVEReturnsTheFieldName() {
    String propertyName = ADMINISTRATIVE.completePropertyName(TYPE, FIELD_NAME);

    assertThat(propertyName, is(equalTo(FIELD_NAME)));
  }

  @Test
  public void propertyNameOfREGUALReturnsInternalNameOfTheTypeAndTheFieldNameSeparatedByAColon() {
    String propertyName = REGULAR.completePropertyName(TYPE, FIELD_NAME);

    String expectedName = String.format("%s_%s", INTERNAL_TYPE_NAME, FIELD_NAME);

    assertThat(propertyName, is(equalTo(expectedName)));
  }

  @Test
  public void propertyNameOfVIRTUALReturnsTheFieldName() {
    String propertyName = VIRTUAL.completePropertyName(TYPE, FIELD_NAME);

    assertThat(propertyName, is(equalTo(FIELD_NAME)));
  }

}
