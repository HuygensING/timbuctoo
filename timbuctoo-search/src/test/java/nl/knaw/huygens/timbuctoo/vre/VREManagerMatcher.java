package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

public class VREManagerMatcher extends TypeSafeMatcher<VREManager> {
  private final VREManager expectedVREManager;

  public VREManagerMatcher(VREManager expectedVREManager) {
    this.expectedVREManager = expectedVREManager;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("VREManager with vres ").appendValue(expectedVREManager.getAllVREs()) //
        .appendText(" and vre ids ").appendValue(expectedVREManager.getAvailableVREIds())//
        .appendText(" and indexes ").appendValue(expectedVREManager.getAllIndexes());
  }

  @Override
  protected void describeMismatchSafely(VREManager item, Description mismatchDescription) {
    mismatchDescription.appendText("VREManager with vres ").appendValue(item.getAllVREs()) //
        .appendText(" and vre ids ").appendValue(item.getAvailableVREIds())//
        .appendText(" and indexes ").appendValue(item.getAllIndexes());
  }

  @Override
  protected boolean matchesSafely(VREManager item) {
    boolean matches = everyItem(isIn(expectedVREManager.getAllIndexes())).matches(item.getAllIndexes());
    matches &= everyItem(isIn(expectedVREManager.getAllVREs())).matches(item.getAllVREs());
    matches &= everyItem(isIn(expectedVREManager.getAvailableVREIds())).matches(item.getAvailableVREIds());

    return matches;
  }

  @Factory
  public static VREManagerMatcher matchesVREManager(VREManager expectedVREManager) {
    return new VREManagerMatcher(expectedVREManager);
  }

}
