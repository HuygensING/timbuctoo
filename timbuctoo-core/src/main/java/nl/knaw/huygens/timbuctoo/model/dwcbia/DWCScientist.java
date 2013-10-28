package nl.knaw.huygens.timbuctoo.model.dwcbia;

import nl.knaw.huygens.timbuctoo.model.Scientist;

public class DWCScientist extends Scientist {
  private String sources;

  public String getSources() {
    return sources;
  }

  public void setSources(String sources) {
    this.sources = sources;
  }
}
