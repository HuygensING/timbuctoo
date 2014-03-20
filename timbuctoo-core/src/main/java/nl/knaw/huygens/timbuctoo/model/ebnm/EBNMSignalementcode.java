package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Signalementcode;

public class EBNMSignalementcode extends Signalementcode {

  private String signalementId;
  private String signalement;

  public String getLabel() {
    return getValue();
  }

  public String getCodeId() {
    return signalementId;
  }

  public void setCodeId(String _id) {
    this.signalementId = _id;
  }

  public String getSignalement() {
    return signalement;
  }

  public void setSignalement(String signalement) {
    this.signalement = signalement;
  }

}
