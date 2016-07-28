package nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlSource;

public class TimbuctooRawCollectionSource implements RmlSource {
  private String rawCollectionName;
  private String vreName;

  public TimbuctooRawCollectionSource(String rawCollectionName, String vreName) {
    this.rawCollectionName = rawCollectionName;
    this.vreName = vreName;
  }

  public String getRawCollectionName() {
    return rawCollectionName;
  }

  public String getVreName() {
    return vreName;
  }
}
