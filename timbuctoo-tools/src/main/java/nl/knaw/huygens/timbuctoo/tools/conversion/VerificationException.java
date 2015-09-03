package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Collection;

public class VerificationException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private String oldId;
  private String newId;
  private Collection<Mismatch> mismatches;

  public VerificationException(String oldId, String newId, Collection<Mismatch> mismatches) {
    this.oldId = oldId;
    this.newId = newId;
    this.mismatches = mismatches;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Object with old id \"%s\" and new id \"%s\" did not match between databases.", oldId, newId));
    sb.append("\n");
    sb.append("The following properties did not match:\n");
    for (Mismatch mismatch : mismatches) {
      sb.append(mismatch);
      sb.append("\n");
    }
    return sb.toString();
  }
}
