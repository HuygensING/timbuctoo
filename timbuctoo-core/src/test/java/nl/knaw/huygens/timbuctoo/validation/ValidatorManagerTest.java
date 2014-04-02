package nl.knaw.huygens.timbuctoo.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.neww.WWRelation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import org.junit.Test;

public class ValidatorManagerTest {

  @Test
  public void testGetValidatorForRelation() {
    ValidatorManager instance = new ValidatorManager(mock(Storage.class));

    Validator<? extends DomainEntity> validator = instance.getValidatorFor(Relation.class);

    assertThat(validator, is(instanceOf(RelationValidator.class)));
  }

  @Test
  public void testGetValidatorForSubClassOfRelation() {
    ValidatorManager instance = new ValidatorManager(mock(Storage.class));

    Validator<? extends DomainEntity> validator = instance.getValidatorFor(WWRelation.class);

    assertThat(validator, is(instanceOf(RelationValidator.class)));
  }

  @Test
  public void testGetValidatorForAnyOtherClass() {
    ValidatorManager instance = new ValidatorManager(mock(Storage.class));

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
