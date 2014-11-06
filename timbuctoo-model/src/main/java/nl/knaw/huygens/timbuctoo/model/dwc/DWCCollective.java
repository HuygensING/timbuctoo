package nl.knaw.huygens.timbuctoo.model.dwc;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Collective;


public class DWCCollective extends Collective {

  private List<String> nameVariants;

  public List<String> getNameVariants() {
    return nameVariants;
  }

  public void setNameVariants(List<String> nameVariants) {
    this.nameVariants = nameVariants;
  }

}
