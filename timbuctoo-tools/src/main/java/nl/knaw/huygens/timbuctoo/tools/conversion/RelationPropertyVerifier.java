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
    if (isSourceIdField(fieldName) || isTargetIdField(fieldName) || isTypeIdField(fieldName)) {

      String mappedValue = oldIdNewIdMap.get(oldValue);
      return Objects.equal(mappedValue, newValue);
    }

    return super.areEqual(fieldName, oldValue, newValue);
  }

  private boolean isTypeIdField(String fieldName) {
    return Objects.equal(fieldName, "typeId");
  }

  private boolean isTargetIdField(String fieldName) {
    return Objects.equal(fieldName, "targetId");
  }

  private boolean isSourceIdField(String fieldName) {
    return Objects.equal(fieldName, "sourceId");
  }
}
