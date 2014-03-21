package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Regiocode;

public class EBNMRegiocode extends Regiocode {

  private String regioId;
  private String regio;

  public String getLabel() {
    return getValue();
  }

  public String getCodeId() {
    return regioId;
  }

  public void setCodeId(String _id) {
    this.regioId = _id;
  }

  public String getRegio() {
    return regio;
  }

  public void setRegio(String regio) {
    this.regio = regio;
  }

}
