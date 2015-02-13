package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.ADMINISTRATIVE;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.VIRTUAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.config.TypeNames;

import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class FieldTypeTest {

  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String INTERNAL_TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final String FIELD_NAME = "fieldName";

  @Test
  public void propertyNameOfADMINISTRATIVEReturnsTheFieldName() {
    String propertyName = ADMINISTRATIVE.propertyName(TYPE, FIELD_NAME);

    assertThat(propertyName, is(equalTo(FIELD_NAME)));
  }

  @Test
  public void propertyNameOfREGUALReturnsInternalNameOfTheTypeAndTheFieldNameSeparatedByAColon() {
    String propertyName = REGULAR.propertyName(TYPE, FIELD_NAME);

    String expectedName = String.format("%s:%s", INTERNAL_TYPE_NAME, FIELD_NAME);

    assertThat(propertyName, is(equalTo(expectedName)));
  }

  @Test
  public void propertyNameOfVIRTUALReturnsTheFieldName() {
    String propertyName = VIRTUAL.propertyName(TYPE, FIELD_NAME);

    assertThat(propertyName, is(equalTo(FIELD_NAME)));
  }

}
