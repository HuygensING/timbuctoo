package nl.knaw.huygens.timbuctoo.tools.conversion;

import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
    /* Do not remove the Object.equals. EqualsBuilder.reflectionEquals does not
     * always recognize two equal Strings.
     */
    return Objects.equals(oldValue, newValue) || EqualsBuilder.reflectionEquals(oldValue, newValue);
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
