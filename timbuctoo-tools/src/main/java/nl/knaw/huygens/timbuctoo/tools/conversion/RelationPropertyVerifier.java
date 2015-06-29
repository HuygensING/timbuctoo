package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import com.google.common.base.Objects;

/**
 * A property verifier to check the equality of the source and target values of a Relation.
 */

public class RelationPropertyVerifier extends PropertyVerifier {
  private Map<String, String> oldIdNewIdMap;

  public RelationPropertyVerifier(Map<String, String> oldIdNewIdMap) {
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  @Override
  protected boolean areEqual(String fieldName, Object oldValue, Object newValue) {
    if (isSourceOrTargetField(fieldName)) {

      String mappedValue = oldIdNewIdMap.get(oldValue);
      return Objects.equal(mappedValue, newValue);
    }

    return super.areEqual(fieldName, oldValue, newValue);
  }

  private boolean isSourceOrTargetField(String fieldName) {
    return Objects.equal(fieldName, "sourceId") || Objects.equal(fieldName, "targetId");
  }
}
