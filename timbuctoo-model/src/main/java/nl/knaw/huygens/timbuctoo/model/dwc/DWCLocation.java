package nl.knaw.huygens.timbuctoo.model.dwc;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

public class DWCLocation extends Location {

  private String country;
  private String remarks;
  private List<String> nameVariants;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setName(String name) {
    PlaceName pn = new PlaceName();
    pn.setCountry(this.country);
    pn.setSettlement(name);
    addName("dutch", pn);
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public List<String> getNameVariants() {
    return nameVariants;
  }

  public void setNameVariants(List<String> nameVariants) {
    this.nameVariants = nameVariants;
  }

}
