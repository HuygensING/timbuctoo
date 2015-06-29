package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class PropertyVerifierTest {
  private static final String NEW_VALUE = "newValue";
  private static final String OLD_VALUE = "oldValue";
  private static final String FIELD_NAME = "fieldName";
  protected PropertyVerifier instance;

  @Before
  public void setup() {
    instance = new PropertyVerifier();
  }

  @Test
  public void addNoMismatchIfTheValuesMatch() {
    // action
    instance.check(FIELD_NAME, NEW_VALUE, NEW_VALUE);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void addAMismatchIfTheOldAndNewValueDoNotMatch() {
    // action
    instance.check(FIELD_NAME, OLD_VALUE, NEW_VALUE);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(true));
    assertThat(instance.getMismatches(), hasSize(1));
  }

  @Test
  public void getMismatchesReturnsTheMismatchesAndClearsTheMismatchCollection() {
    // setup
    instance.check(FIELD_NAME, OLD_VALUE, NEW_VALUE);

    // action
    Collection<Mismatch> mismatches = instance.getMismatches();

    // verify
    assertThat(mismatches, hasSize(1));

    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), is(emptyCollectionOf(Mismatch.class)));
  }
}
