package nl.knaw.huygens.repository.model.atlg;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Keyword;

@DocumentTypeName("atlgkeyword")
public class ATLGKeyword extends Keyword {

  private String label;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}
