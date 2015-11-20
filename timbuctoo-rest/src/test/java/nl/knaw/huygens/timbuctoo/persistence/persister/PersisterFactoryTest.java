package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PersisterFactoryTest {

  private PersisterFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PersisterFactory();
  }

  @Test
  public void forActionTypeCreatesAnAddPersisterWhenTheActionTypeIsADD() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.ADD, AddPersister.class);
  }


  @Test
  public void forActionTypeCreatesAModPersisterWhenTheActionTypeIsMOD() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.MOD, ModPersister.class);
  }

  @Test
  public void forActionTypeCreatesANoOpPersisterWhenTheActionTypeIsDEL() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.DEL, NoOpPersister.class);
  }

  @Test
  public void forActionTypeCreatesANoOpPersisterWhenTheActionTypeIsEND() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.END, NoOpPersister.class);
  }

  private void forActionTypeCreatesPersisterOfType(ActionType actionType, Class<? extends Persister> persisterType) {
    // action
    Persister persister = instance.forActionType(actionType);

    // verify
    assertThat(persister, is(instanceOf(persisterType)));
  }
}
