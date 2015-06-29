package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

public class PropertyVerifier {
  private List<Mismatch> misMatches;

  public PropertyVerifier() {
    misMatches = Lists.newArrayList();
  }

  public void check(String fieldName, Object oldValue, Object newValue) {
    if (!areEqual(fieldName, oldValue, newValue)) {
      addMismatch(fieldName, oldValue, newValue);
    }
  }

  protected boolean areEqual(String fieldName, Object oldValue, Object newValue) {
    return Objects.equals(oldValue, newValue);
  }

  private void addMismatch(String fieldName, Object oldValue, Object newValue) {
    misMatches.add(new Mismatch(fieldName, oldValue, newValue));
  }

  public boolean hasInconsistentProperties() {
    return !misMatches.isEmpty();
  }

  public Collection<Mismatch> getMismatches() {
    ArrayList<Mismatch> returnValue = Lists.newArrayList(misMatches);

    misMatches.clear();

    return returnValue;
  }

}
