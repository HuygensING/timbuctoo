package nl.knaw.huygens.timbuctoo.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Test;

public class ValidatorManagerTest {

  @Test
  public void testGetValidatorForRelation() {
    ValidatorManager instance = new ValidatorManager();

    Validator validator = instance.getValidatorFor(Relation.class);

    assertThat(validator, is(instanceOf(RelationValidator.class)));
  }

  @Test
  public void testGetValidatorForAnyOtherClass() {
    ValidatorManager instance = new ValidatorManager();

    Validator validator = instance.getValidatorFor(TestDomainEntity.class);

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
