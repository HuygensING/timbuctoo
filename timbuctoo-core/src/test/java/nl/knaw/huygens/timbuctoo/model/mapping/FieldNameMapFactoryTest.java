package nl.knaw.huygens.timbuctoo.model.mapping;

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

import nl.knaw.huygens.timbuctoo.model.Entity;
import org.junit.Before;
import org.junit.Test;
import test.model.MappingExample;
import test.model.projecta.ProjectAMappingExample;

import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class FieldNameMapFactoryTest {

  public static final Class<MappingExample> TYPE = MappingExample.class;
  public static final Class<ProjectAMappingExample> SUB_TYPE = ProjectAMappingExample.class;
  private FieldNameMapFactory instance;

  @Before
  public void setup() {
    instance = new FieldNameMapFactory();
  }

  @Test
  public void createOnlyMapsTheFieldsThatHaveBothRepresentations() throws Exception {
    // setup
    String idKey = Entity.ID_PROPERTY_NAME;
    String idValue = Entity.INDEX_FIELD_ID;

    String expectedKey1 = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;
    String expectedValue1 = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;

    String expectedKey2 = MappingExample.CLIENT_FIELD_FIRST_NON_SORTABLE;
    String expectedValue2 = MappingExample.FIRST_NON_SORTABLE;

    // action
    FieldNameMap fieldNameMap = instance.create(CLIENT, INDEX, TYPE);

    // verify
    assertThat(fieldNameMap.getFromNames(), containsInAnyOrder(expectedKey1, expectedKey2, idKey));
    assertThat(fieldNameMap.translate(expectedKey1), is(expectedValue1));
    assertThat(fieldNameMap.translate(expectedKey2), is(expectedValue2));
    assertThat(fieldNameMap.translate(idKey), is(idValue));
  }

  @Test
  public void createMapsFieldsFromParents() throws Exception {
    // setup
    String idKey = Entity.INDEX_FIELD_ID;
    String idValue = Entity.ID_PROPERTY_NAME;

    String expectedKey1 = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;
    String expectedValue1 = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;

    // action
    FieldNameMap fieldNameMap = instance.create(INDEX, CLIENT, SUB_TYPE);

    // verify
    hasKeyWithValue(fieldNameMap, expectedKey1, expectedValue1);
    hasKeyWithValue(fieldNameMap, idKey, idValue);
  }

  @Test
  public void createMapsDerivedProperties() throws Exception {
    // setup
    String expectedKey1 = ProjectAMappingExample.DERIVED1_INDEX;
    String expectedKey2 = ProjectAMappingExample.DERIVED2_INDEX;
    String expectedValue1 = ProjectAMappingExample.DERIVED_1;
    String expectedValue2 = ProjectAMappingExample.DERIVED_2;

    // action
    FieldNameMap fieldNameMap = instance.create(INDEX, CLIENT, SUB_TYPE);

    // verify
    hasKeyWithValue(fieldNameMap, expectedKey1, expectedValue1);
    hasKeyWithValue(fieldNameMap, expectedKey2, expectedValue2);
  }

  @Test
  public void createMapsVirtualProperties() throws Exception {
    // setup
    String expectedKey = ProjectAMappingExample.VIRTUAL_INDEX;
    String expectedValue = ProjectAMappingExample.VIRTUAL_CLIENT;

    // action
    FieldNameMap fieldNameMap = instance.create(INDEX, CLIENT, SUB_TYPE);

    // verify
    hasKeyWithValue(fieldNameMap, expectedKey, expectedValue);
  }

  private void hasKeyWithValue(FieldNameMap fieldNameMap, String expectedKey, String expectedValue) {
    assertThat(fieldNameMap.getFromNames(), hasItem(expectedKey));
    assertThat(fieldNameMap.translate(expectedKey), is(expectedValue));
  }


}
