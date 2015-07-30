package nl.knaw.huygens.timbuctoo.model.mapping;

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
    String expectedKey1 = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;
    String expectedValue1 = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;

    String expectedKey2 = MappingExample.CLIENT_FIELD_FIRST_NON_SORTABLE;
    String expectedValue2 = MappingExample.FIRST_NON_SORTABLE;

    // action
    FieldNameMap fieldNameMap = instance.create(CLIENT, INDEX, TYPE);

    // verify
    assertThat(fieldNameMap.getKeys(), containsInAnyOrder(expectedKey1, expectedKey2));
    assertThat(fieldNameMap.get(expectedKey1), is(expectedValue1));
    assertThat(fieldNameMap.get(expectedKey2), is(expectedValue2));
  }

  @Test
  public void createMapsFieldsFromParents() throws Exception {
    // setup
    String expectedKey1 = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;
    String expectedValue1 = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;

    // action
    FieldNameMap fieldNameMap = instance.create(INDEX, CLIENT, SUB_TYPE);

    // verify
    // verify
    hasKeyWithValue(fieldNameMap, expectedKey1, expectedValue1);
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

  private void hasKeyWithValue(FieldNameMap fieldNameMap, String expectedKey, String expectedValue) {
    assertThat(fieldNameMap.getKeys(), hasItem(expectedKey));
    assertThat(fieldNameMap.get(expectedKey), is(expectedValue));
  }


}
