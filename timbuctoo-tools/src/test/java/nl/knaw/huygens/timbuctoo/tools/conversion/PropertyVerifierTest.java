package nl.knaw.huygens.timbuctoo.tools.conversion;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class PropertyVerifierTest {
  private static final String VALUE = "newValue";
  private static final String OTHER_VALUE = "oldValue";
  private static final String FIELD_NAME = "fieldName";
  protected PropertyVerifier instance;

  @Before
  public void setup() {
    instance = new PropertyVerifier();
  }

  @Test
  public void addNoMismatchIfTheValuesMatch() {
    // action
    instance.check(FIELD_NAME, VALUE, VALUE);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void addsNoMismatchWhenTwoObjectsWithoutImplementedEqualsWithTheSameValue() {
    // setup
    ObjectWithOutEquals first = new ObjectWithOutEquals(VALUE, OTHER_VALUE);
    ObjectWithOutEquals second = new ObjectWithOutEquals(VALUE, OTHER_VALUE);

    // action
    instance.check(FIELD_NAME, first, second);

    // verify
    assertThat(first, is(not(second)));
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void addAMismatchIfTheOldAndNewValueDoNotMatch() {
    // action
    instance.check(FIELD_NAME, OTHER_VALUE, VALUE);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(true));
    assertThat(instance.getMismatches(), hasSize(1));
  }

  @Test
  public void getMismatchesReturnsTheMismatchesAndClearsTheMismatchCollection() {
    // setup
    instance.check(FIELD_NAME, OTHER_VALUE, VALUE);

    // action
    Collection<Mismatch> mismatches = instance.getMismatches();

    // verify
    assertThat(mismatches, hasSize(1));

    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), is(emptyCollectionOf(Mismatch.class)));
  }

  private class ObjectWithOutEquals {

    private String value;
    private String otherValue;

    public ObjectWithOutEquals(String value, String otherValue) {

      this.value = value;
      this.otherValue = otherValue;
    }

    private String getValue() {
      return value;
    }

    private void setValue(String value) {
      this.value = value;
    }

    private String getOtherValue() {
      return otherValue;
    }

    private void setOtherValue(String otherValue) {
      this.otherValue = otherValue;
    }
  }

}
