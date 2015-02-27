package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

public class IdGeneratorTest {

  private IdGenerator instance;

  @Before
  public void setUp() {
    instance = new IdGenerator();
  }

  @Test
  public void nextIdForCreatesAnIdWithAPrefixOfTheSystemEntity() {
    verifyPrefixOfNextIdFor(TestSystemEntityWrapper.class, TestSystemEntityWrapper.ID_PREFIX);
  }

  @Test
  public void nextIdForCreatesAnIdWithAPrefixOfThePrimitiveDomainEntityWhenASubTypeIsInserted() {
    verifyPrefixOfNextIdFor(SubADomainEntity.class, BaseDomainEntity.ID_PREFIX);
  }

  @Test
  public void nextIdForCreatesAnIdWithAPrefixOfThePrimitiveDomainEntityWhenThePrimitiveTypeIsInserted() {
    verifyPrefixOfNextIdFor(BaseDomainEntity.class, BaseDomainEntity.ID_PREFIX);
  }

  private void verifyPrefixOfNextIdFor(Class<? extends Entity> type, String expectedPrefix) {
    // action
    String id = instance.nextIdFor(type);

    // verify
    assertThat(id, startsWith(expectedPrefix));
  }

  @Ignore("Think of the implementation to handle legacy id's")
  @Test
  public void nextIdForCreatesAnIdWith12NumbersAfterThePrefix() {
    fail("Yet to be implemented");
  }

  @Test
  public void nextIdForCreatesANewIdEachTimeItIsCalled() {
    // setup
    Class<TestSystemEntityWrapper> type = TestSystemEntityWrapper.class;

    // action
    String id1 = instance.nextIdFor(type);
    String id2 = instance.nextIdFor(type);

    // verify
    assertThat(id1, is(not(equalTo(id2))));
  }
}
