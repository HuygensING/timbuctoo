package nl.knaw.huygens.timbuctoo.model.mapping;

import org.junit.Ignore;
import org.junit.Test;
import test.model.MappingExample;

import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.INDEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

@Ignore
public class DomainEntityFieldNameMapFactoryTest {

  public static final Class<MappingExample> TYPE = MappingExample.class;

  @Test
  public void createOnlyMapsTheFieldsNames() {
    // setup
    String expectedKey = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;
    String expectedValue = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;

    DomainEntityFieldNameMapFactory instance = new DomainEntityFieldNameMapFactory();

    // action
    FieldNameMap fieldNameMap = instance.create(INDEX, CLIENT, TYPE);

    // verify
    assertThat(fieldNameMap.getKeys(), hasItem(expectedKey));
    assertThat(fieldNameMap.get(expectedKey), is(expectedValue));
  }

  @Test
  public void createOnlyMapsTheFieldsThatHaveBothRepresentations() {
    // setup
    String expectedKey = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;
    String unExpectedKey = MappingExample.FIELD_WITHOUT_GETTER;
    String expectedValue = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;
    DomainEntityFieldNameMapFactory instance = new DomainEntityFieldNameMapFactory();

    // action
    FieldNameMap fieldNameMap = instance.create(CLIENT, INDEX, TYPE);

    // verify
    assertThat(fieldNameMap.getKeys(), hasItem(expectedKey));
    assertThat(fieldNameMap.getKeys(), not(hasItem(unExpectedKey)));
    assertThat(fieldNameMap.get(expectedKey), is(expectedValue));
  }

  @Test
  public void createAlsoMapsFieldsFromParents(){
    fail("Yet to be implemented");
  }


}
