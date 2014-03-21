package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Periode;

public class EBNMPeriode extends Periode {

  private String periodeId;
  private String periode;

  public String getLabel() {
    return getValue();
  }

  public String getCodeId() {
    return periodeId;
  }

  public void setCodeId(String _id) {
    this.periodeId = _id;
  }

  public String getPeriode() {
    return periode;
  }

  public void setPeriode(String periode) {
    this.periode = periode;
  }

}
