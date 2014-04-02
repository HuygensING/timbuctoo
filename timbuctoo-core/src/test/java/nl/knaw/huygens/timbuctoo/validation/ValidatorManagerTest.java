package nl.knaw.huygens.timbuctoo.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.neww.WWRelation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import org.junit.Before;
import org.junit.Test;

public class ValidatorManagerTest {

  private ValidatorManager instance;

  @Before
  public void setUp() {
    instance = new ValidatorManager(mock(Storage.class), mock(TypeRegistry.class));
  }

  @Test
  public void testGetValidatorForRelation() {
    Validator<? extends DomainEntity> validator = instance.getValidatorFor(Relation.class);

    assertThat(validator, is(instanceOf(RelationValidator.class)));
  }

  @Test
  public void testGetValidatorForSubClassOfRelation() {
    Validator<? extends DomainEntity> validator = instance.getValidatorFor(WWRelation.class);

    assertThat(validator, is(instanceOf(RelationValidator.class)));
  }

  @Test
  public void testGetValidatorForAnyOtherClass() {
    Validator<? extends DomainEntity> validator = instance.getValidatorFor(TestDomainEntity.class);

    assertThat(validator, is(instanceOf(NoOpValidator.class)));
  }

  private static class TestDomainEntity extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
