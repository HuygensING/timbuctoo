package nl.knaw.huygens.timbuctoo.storage.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Test;

import test.model.projecta.SubADomainEntity;

public class EntityInstantiatorTest {
  private static final Class<SubADomainEntity> ENTITY_TYPE = SubADomainEntity.class;

  @Test
  public void createInstanceForReturnAndInstanceForTheType() throws Exception {
    // setup
    EntityInstantiator instance = new EntityInstantiator();

    // action
    SubADomainEntity entity = instance.createInstanceOf(ENTITY_TYPE);

    // verify
    assertThat(entity, is(notNullValue()));
  }

  @Test(expected = InstantiationException.class)
  public void createInstanceThrowsAnInstationExceptionWhenCreateDoes() throws Exception {
    EntityInstantiator instance = new EntityInstantiator() {
      @Override
      protected <T extends Entity> T create(Class<T> type) throws InstantiationException, IllegalAccessException {
        throw new InstantiationException();
      }
    };

    instance.createInstanceOf(ENTITY_TYPE);

  }

  @Test(expected = InstantiationException.class)
  public void createInstanceThrowsAnInstationExceptionWhenCreateThrowsAnIllegalAccessException() throws Exception {
    EntityInstantiator instance = new EntityInstantiator() {
      @Override
      protected <T extends Entity> T create(Class<T> type) throws InstantiationException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };

    instance.createInstanceOf(ENTITY_TYPE);

  }
}
